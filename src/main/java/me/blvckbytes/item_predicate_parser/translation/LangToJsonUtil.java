package me.blvckbytes.item_predicate_parser.translation;

import com.google.gson.JsonObject;

public class LangToJsonUtil {

  public static JsonObject convertLangContentsToJsonObject(String langContents) {
    var result = new JsonObject();

    // I believe it was two back-to-back '#'-s...
    var commentMarker = "##";
    var lastNewlineIndex = -1;

    do {
      var lineBeginIndex = lastNewlineIndex + 1;
      var nextNewlineIndex = langContents.indexOf('\n', lineBeginIndex);
      lastNewlineIndex = nextNewlineIndex;

      // Consecutive newlines - empty line
      if (lineBeginIndex == nextNewlineIndex)
        continue;

      var commentBeginIndex = langContents.indexOf(commentMarker, lineBeginIndex);

      // Fully commented line
      if (commentBeginIndex == 0)
        continue;

      var pairSeparatorIndex = langContents.indexOf('=', lineBeginIndex);

      // Make sure that there's no comment before the pair-separator character
      if (pairSeparatorIndex <= 0 || (commentBeginIndex > 0 && commentBeginIndex <= pairSeparatorIndex))
        continue;

      var key = langContents.substring(lineBeginIndex, pairSeparatorIndex);

      String value;

      // Make sure that the next comment is still on the same line
      if (commentBeginIndex > 0 && commentBeginIndex < nextNewlineIndex)
        value = langContents.substring(pairSeparatorIndex + 1, commentBeginIndex);
      else
        value = langContents.substring(pairSeparatorIndex + 1, nextNewlineIndex);

      result.addProperty(key, value);
    } while (lastNewlineIndex >= 0);

    return result;
  }
}
