package me.blvckbytes.storage_query.parse;

import java.util.ArrayList;

public record SubstringIndices(int start, int end) {

  public static final char[] LANGUAGE_FILE_DELIMITERS = { '-', ' ', '_' };
  public static final char[] SEARCH_PATTERN_DELIMITERS = { '-' };
  public static final char[] FREE_TEXT_DELIMITERS = { ' ' };

  public int length() {
    return (end - start) + 1;
  }

  public static ArrayList<SubstringIndices> forString(String input, char[] delimiters) {
    var result = new ArrayList<SubstringIndices>();
    var inputLength = input.length();

    int nextPartBeginning = 0;
    boolean encounteredNonDelimiter = false;

    for (int i = 0; i < inputLength; ++i) {
      var currentChar = input.charAt(i);
      boolean isDelimiter = false;

      for (char delimiter : delimiters) {
        if (currentChar == delimiter) {
          isDelimiter = true;
          break;
        }
      }

      if (isDelimiter) {
        if (i != 0 && encounteredNonDelimiter)
          result.add(new SubstringIndices(nextPartBeginning, i - 1));

        nextPartBeginning = i + 1;
        encounteredNonDelimiter = false;
        continue;
      }

      else
        encounteredNonDelimiter = true;

      if (i == inputLength - 1)
        result.add(new SubstringIndices(nextPartBeginning, i));
    }

    return result;
  }

  private static int relativeIndexOf(String contained, SubstringIndices containedIndices, String container, SubstringIndices containerIndices) {
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

        if (Character.toLowerCase(container.charAt(containerIndex)) != Character.toLowerCase(contained.charAt(containedIndex))) {
          didMatch = false;
          break;
        }
      }

      if (didMatch)
        return containerOffset;
    }

    return -1;
  }

  public static void matchQuerySubstrings(
    String query,
    ArrayList<SubstringIndices> pendingQuerySubstrings,
    String text,
    ArrayList<SubstringIndices> remainingTextSubstrings
  ) {
    for (var pendingQuerySubstringsIterator = pendingQuerySubstrings.iterator(); pendingQuerySubstringsIterator.hasNext();) {
      var pendingQuerySubstring = pendingQuerySubstringsIterator.next();
      boolean didQuerySubstringMatch = false;

      for (var remainingTextSubstringIndex = 0; remainingTextSubstringIndex < remainingTextSubstrings.size(); ++remainingTextSubstringIndex) {
        var remainingTextSubstring = remainingTextSubstrings.get(remainingTextSubstringIndex);
        var relativeIndex = SubstringIndices.relativeIndexOf(query, pendingQuerySubstring, text, remainingTextSubstring);

        if (relativeIndex < 0)
          continue;

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
              remainingTextSubstring.end()
            )
          );
        }

        // Remainder previous to match (add afterwards to ensure proper order)
        if (relativeIndex != 0) {
          remainingTextSubstrings.add(
            remainingTextSubstringIndex,
            new SubstringIndices(
              remainingTextSubstring.start(),
              remainingTextSubstring.start() + relativeIndex - 1
            )
          );
        }

        didQuerySubstringMatch = true;
        break;
      } // Remaining text substrings

      if (didQuerySubstringMatch)
        pendingQuerySubstringsIterator.remove();
    } // Pending query substrings
  }
}
