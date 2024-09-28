package me.blvckbytes.item_predicate_parser.parse;

import me.blvckbytes.item_predicate_parser.token.Token;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public record SubstringIndices(
  int start,
  int end,
  boolean isNegated,
  boolean isPatternWildcardChar
) {
  public static final char SEARCH_PATTERN_DELIMITER = '-';
  public static final char FREE_TEXT_DELIMITER = ' ';

  private static final char PATTERN_WILDCARD_CHAR = '?';
  private static final char PATTERN_NEGATION_CHAR = '!';

  public SubstringIndices(int start, int end) {
    this(start, end, false, false);
  }

  private static SubstringIndices makePossiblyNegatable(int start, int end, String text) {
    var isPatternWildcardChar = start == end && text.charAt(start) == PATTERN_WILDCARD_CHAR;

    if (start != end && text.charAt(start) == PATTERN_NEGATION_CHAR)
      return new SubstringIndices(start + 1, end, true, false);

    return new SubstringIndices(start, end, false, isPatternWildcardChar );
  }

  public int length() {
    return (end - start) + 1;
  }

  /**
   * @param token If provided, checks for search pattern wildcard presence (throws on duplicate or only)
   */
  public static ArrayList<SubstringIndices> forString(
    @Nullable Token token,
    String input,
    char delimiter
  ) {
    var result = new ArrayList<SubstringIndices>();
    var inputLength = input.length();

    int nextPartBeginning = 0;
    boolean encounteredNonDelimiter = false;
    boolean encounteredSearchPatternWildcard = false;
    boolean encounteredNonSearchPatternWildcard = false;

    for (int i = 0; i < inputLength; ++i) {
      var currentChar = input.charAt(i);
      var isDelimiter = currentChar == delimiter;

      if (!isDelimiter) {
        encounteredNonDelimiter = true;

        if (i != inputLength - 1)
          continue;
      }

      if (encounteredNonDelimiter) {
        var nextIndices = SubstringIndices.makePossiblyNegatable(nextPartBeginning, isDelimiter ? i - 1 : i, input);

        if (nextIndices.isPatternWildcardChar) {
          if (token != null) {
            if (encounteredSearchPatternWildcard)
              throw new ItemPredicateParseException(token, ParseConflict.MULTIPLE_SEARCH_PATTERN_WILDCARDS);
            encounteredSearchPatternWildcard = true;
          }
        }
        else
          encounteredNonSearchPatternWildcard = true;

        result.add(nextIndices);
      }

      nextPartBeginning = i + 1;
      encounteredNonDelimiter = false;
    }

    if (encounteredSearchPatternWildcard && !encounteredNonSearchPatternWildcard)
      throw new ItemPredicateParseException(token, ParseConflict.ONLY_SEARCH_PATTERN_WILDCARD);

    return result;
  }

  public record IndexOfResult(
    int beginInContainer,
    int numberOfContainerChars
  ) {}

  private static IndexOfResult relativeIndexOf(String contained, SubstringIndices containedIndices, String container, SubstringIndices containerIndices) {
    var containerIndicesLength = containerIndices.length();
    var containedIndicesLength = containedIndices.length();

    if (containerIndicesLength < containedIndicesLength)
      return new IndexOfResult(-1, 0);

    /*
      0: A B C
      1:   A B C
      2:     A B C
         A B C D E
     */
    var highestOffset = containerIndicesLength - containedIndicesLength;

    for (int containerOffset = 0; containerOffset <= highestOffset; ++containerOffset) {
      boolean didMatch = true;

      // As color-sequences are skipped on the outer-loop's counter, keep an internal
      // backup to respond the very front of the sequence, including said sequences.
      var initialContainerOffset = containerOffset;

      for (int containedOffset = 0; containedOffset < containedIndicesLength; ++containedOffset) {
        var containerIndex = containerIndices.start + containedOffset + containerOffset;
        var containedIndex = containedIndices.start + containedOffset;
        var containerChar = container.charAt(containerIndex);
        var containedChar = contained.charAt(containedIndex);

        while (containerChar == '§' && containerOffset < highestOffset) {
          var nextContainerChar = container.charAt(containerIndex + 1);

          if (
            (nextContainerChar >= '0' && nextContainerChar <= '9')
            || (nextContainerChar >= 'a' && nextContainerChar <= 'f')
            || (nextContainerChar >= 'k' && nextContainerChar <= 'o')
            || nextContainerChar == 'r'
          ) {
            containerOffset += 2;
            containerIndex = containerIndices.start + containedOffset + containerOffset;
            containerChar = container.charAt(containerIndex);
          }

          else
            break;
        }

        if (Character.toLowerCase(containerChar) != Character.toLowerCase(containedChar)) {
          didMatch = false;
          break;
        }
      }

      if (didMatch)
        return new IndexOfResult(
          initialContainerOffset,
          containerOffset - initialContainerOffset + containedIndicesLength
        );
    }

    return new IndexOfResult(-1, 0);
  }

  /**
   * @return Whether a pattern wildcard char has been encountered
   */
  public static boolean matchQuerySubstrings(
    String query,
    ArrayList<SubstringIndices> pendingQuerySubstrings,
    String text,
    ArrayList<SubstringIndices> remainingTextSubstrings
  ) {
    boolean hasSearchPatternWildcard = false;

    for (var pendingQuerySubstringsIterator = pendingQuerySubstrings.iterator(); pendingQuerySubstringsIterator.hasNext();) {
      var pendingQuerySubstring = pendingQuerySubstringsIterator.next();

      if (pendingQuerySubstring.isPatternWildcardChar) {
        hasSearchPatternWildcard = true;
        pendingQuerySubstringsIterator.remove();
      }

      boolean didQuerySubstringMatch = false;

      for (var remainingTextSubstringIndex = 0; remainingTextSubstringIndex < remainingTextSubstrings.size(); ++remainingTextSubstringIndex) {
        var remainingTextSubstring = remainingTextSubstrings.get(remainingTextSubstringIndex);
        var indexResult = SubstringIndices.relativeIndexOf(query, pendingQuerySubstring, text, remainingTextSubstring);
        var relativeIndex = indexResult.beginInContainer;

        if (relativeIndex < 0)
          continue;

        // Do also remove for negated query substrings, so that no other syllable can match on it

        remainingTextSubstrings.remove(remainingTextSubstringIndex);

        /*
          Cases:
          vvv-----
          ABCDEFGH

          ---vvv--
          ABCDEFGH

          -----vvv
          ABCDEFGH
         */

        var remainingTextSubstringLength = remainingTextSubstring.length();
        var substringLength = indexResult.numberOfContainerChars;

        // Remainder after match
        if (relativeIndex != remainingTextSubstringLength - substringLength) {
          remainingTextSubstrings.add(
            remainingTextSubstringIndex,
            new SubstringIndices(
              remainingTextSubstring.start() + relativeIndex + substringLength,
              remainingTextSubstring.end(),
              false, false
            )
          );
        }

        // Remainder previous to match (add afterwards to ensure proper order)
        if (relativeIndex != 0) {
          remainingTextSubstrings.add(
            remainingTextSubstringIndex,
            new SubstringIndices(
              remainingTextSubstring.start(),
              remainingTextSubstring.start() + relativeIndex - 1,
              false, false
            )
          );
        }

        didQuerySubstringMatch = true;
        break;
      } // Remaining text substrings

      if (didQuerySubstringMatch) {
        // Do not remove negated query substrings that matched, as to keep the result a mismatch
        if (pendingQuerySubstring.isNegated)
          continue;

        pendingQuerySubstringsIterator.remove();
        continue;
      }

      // Remove negated query substrings which didn't find a match, as to allow the result to become a match
      if (pendingQuerySubstring.isNegated)
        pendingQuerySubstringsIterator.remove();

    } // Pending query substrings

    return hasSearchPatternWildcard;
  }
}
