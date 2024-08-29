package me.blvckbytes.storage_query.parse;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public record SubstringIndices(
  int start,
  int end,
  boolean isNegated,
  boolean isPatternWildcardChar
) {
  public static final char[] LANGUAGE_FILE_DELIMITERS = { '-', ' ', '_' };
  public static final char[] SEARCH_PATTERN_DELIMITERS = { '-' };
  public static final char[] FREE_TEXT_DELIMITERS = { ' ' };

  private static final char PATTERN_WILDCARD_CHAR = '?';
  private static final char PATTERN_NEGATION_CHAR = '!';

  public SubstringIndices(int start, int end) {
    this(start, end, false, false);
  }

  private static SubstringIndices makePossiblyNegatable(int start, int end, String text) {
    var isPatternWildcardChar = start == end && text.charAt(start) == PATTERN_WILDCARD_CHAR;

    if (start != end && text.charAt(start) == PATTERN_NEGATION_CHAR)
      return new SubstringIndices(start + 1, end, true, isPatternWildcardChar );

    return new SubstringIndices(start, end, false, isPatternWildcardChar );
  }

  public int length() {
    return (end - start) + 1;
  }

  /**
   * @param argumentIndex If provided, checks for search pattern wildcard presence (throws on duplicate or only)
   */
  public static ArrayList<SubstringIndices> forString(
    @Nullable Integer argumentIndex,
    String input,
    char[] delimiters
  ) {
    var result = new ArrayList<SubstringIndices>();
    var inputLength = input.length();

    int nextPartBeginning = 0;
    boolean encounteredNonDelimiter = false;
    boolean encounteredSearchPatternWildcard = false;
    boolean encounteredNonSearchPatternWildcard = false;

    for (int i = 0; i < inputLength; ++i) {
      var currentChar = input.charAt(i);
      boolean isDelimiter = false;

      for (char delimiter : delimiters) {
        if (currentChar == delimiter) {
          isDelimiter = true;
          break;
        }
      }

      if (!isDelimiter) {
        encounteredNonDelimiter = true;

        if (i != inputLength - 1)
          continue;
      }

      if (encounteredNonDelimiter) {
        var nextIndices = SubstringIndices.makePossiblyNegatable(nextPartBeginning, isDelimiter ? i - 1 : i, input);

        if (nextIndices.isPatternWildcardChar) {
          if (argumentIndex != null) {
            if (encounteredSearchPatternWildcard)
              throw new ArgumentParseException(argumentIndex, ParseConflict.MULTIPLE_SEARCH_PATTERN_WILDCARDS);
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
      throw new ArgumentParseException(argumentIndex, ParseConflict.ONLY_SEARCH_PATTERN_WILDCARD);

    return result;
  }

  private static int relativeIndexOf(String contained, SubstringIndices containedIndices, String container, SubstringIndices containerIndices) {
    // TODO: Text search should ignore color sequences
    var containerIndicesLength = containerIndices.length();
    var containedIndicesLength = containedIndices.length();

    if (containerIndicesLength < containedIndicesLength)
      return -1;

    /*
      0: A B C
      1:   A B C
      2:     A B C
         A B C D E
     */
    var highestOffset = containerIndicesLength - containedIndicesLength;

    for (int containerOffset = 0; containerOffset <= highestOffset; ++containerOffset) {
      boolean didMatch = true;

      for (int containedOffset = 0; containedOffset < containedIndicesLength; ++containedOffset) {
        var containerIndex = containerIndices.start + containedOffset + containerOffset;
        var containedIndex = containedIndices.start + containedOffset;

        var containerChar = container.charAt(containerIndex);

        if (Character.toLowerCase(containerChar) != Character.toLowerCase(contained.charAt(containedIndex))) {
          didMatch = false;
          break;
        }
      }

      if (didMatch)
        return containerOffset;
    }

    return -1;
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
    // TODO: If a syllable equals to a wildcard, disregard direct matches
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
        var relativeIndex = SubstringIndices.relativeIndexOf(query, pendingQuerySubstring, text, remainingTextSubstring);

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
        var pendingQuerySubstringLength = pendingQuerySubstring.length();

        // Remainder after match
        if (relativeIndex != remainingTextSubstringLength - pendingQuerySubstringLength) {
          remainingTextSubstrings.add(
            remainingTextSubstringIndex,
            new SubstringIndices(
              remainingTextSubstring.start() + relativeIndex + pendingQuerySubstringLength,
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
