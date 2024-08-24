package me.blvckbytes.storage_query.translation;

import java.util.ArrayList;
import java.util.List;

public record SubstringIndices(int start, int end) {

  public static final char[] LANGUAGE_FILE_DELIMITERS = {'-', ' ', '_'};
  public static final char[] INPUT_DELIMITERS = { '-' };

  public int length() {
    return (end - start) + 1;
  }

  public static List<SubstringIndices> forString(String input, char[] delimiters) {
    var result = new ArrayList<SubstringIndices>();
    var inputLength = input.length();

    int nextPartBeginning = 0;

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
        if (i != 0)
          result.add(new SubstringIndices(nextPartBeginning, i - 1));

        nextPartBeginning = i + 1;
        continue;
      }

      if (i == inputLength - 1)
        result.add(new SubstringIndices(nextPartBeginning, i));
    }

    return result;
  }

  public static int relativeIndexOf(String contained, SubstringIndices containedIndices, String container, SubstringIndices containerIndices) {
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

        if (container.charAt(containerIndex) != contained.charAt(containedIndex)) {
          didMatch = false;
          break;
        }
      }

      if (didMatch)
        return containerOffset;
    }

    return -1;
  }
}
