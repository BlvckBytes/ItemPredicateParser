package me.blvckbytes.storage_query.parse;

import com.google.common.primitives.Ints;
import me.blvckbytes.storage_query.token.Token;
import me.blvckbytes.storage_query.token.IntegerToken;
import me.blvckbytes.storage_query.token.QuotedStringToken;
import me.blvckbytes.storage_query.token.UnquotedStringToken;

import java.util.ArrayList;
import java.util.List;

public class TokenParser {

  public static List<Token> parseTokens(String[] args) {
    var result = new ArrayList<Token>();

    var stringBeginArgumentIndex = -1;
    var stringContents = new StringBuilder();

    for (var argumentIndex = 0; argumentIndex < args.length; ++argumentIndex) {
      var arg = args[argumentIndex];
      var argLength = arg.length();

      if (argLength == 0) {
        result.add(new UnquotedStringToken(argumentIndex, ""));
        continue;
      }

      var firstChar = arg.charAt(0);

      if (firstChar == '"') {
        var terminationIndex = arg.indexOf('"', 1);

        // Argument contains both the start- and end-marker
        if (terminationIndex > 0) {
          if (arg.indexOf('"', terminationIndex + 1) != -1)
            throw new ArgumentParseException(argumentIndex, ParseConflict.MALFORMED_STRING_ARGUMENT);

          result.add(new QuotedStringToken(argumentIndex, arg.substring(1, argLength - 1)));
          continue;
        }

        // Contains only one double-quote, which is leading

        // Terminated a string which contains a trailing whitespace (valid use-case)
        if (stringBeginArgumentIndex != -1) {
          if (argLength != 1)
            throw new ArgumentParseException(argumentIndex, ParseConflict.MALFORMED_STRING_ARGUMENT);

          stringContents.append(' ');
          result.add(new QuotedStringToken(stringBeginArgumentIndex, stringContents.toString()));
          stringBeginArgumentIndex = -1;
          stringContents.setLength(0);
          continue;
        }

        // Started a string which contains a leading whitespace (valid use-case)
        if (argLength == 1) {
          stringBeginArgumentIndex = argumentIndex;
          continue;
        }

        // Multi-arg string beginning
        stringBeginArgumentIndex = argumentIndex;
        stringContents.append(arg, 1, argLength);
        continue;
      }

      if (arg.charAt(argLength - 1) == '"') {
        if (stringBeginArgumentIndex == -1)
          throw new ArgumentParseException(argumentIndex, ParseConflict.MALFORMED_STRING_ARGUMENT);

        // Multi-arg string termination
        stringContents.append(' ').append(arg, 0, argLength - 1);
        result.add(new QuotedStringToken(stringBeginArgumentIndex, stringContents.toString()));
        stringBeginArgumentIndex = -1;
        stringContents.setLength(0);
        continue;
      }

      // Within string
      if (stringBeginArgumentIndex >= 0) {
        stringContents.append(' ').append(arg);
        continue;
      }

      if (firstChar == '*' && argLength == 1) {
        result.add(new IntegerToken(argumentIndex, null));
        continue;
      }

      // No names will have a leading digit; expect integer
      if (Character.isDigit(firstChar)) {
        var numericArgument = Ints.tryParse(arg);

        if (numericArgument == null)
          throw new ArgumentParseException(argumentIndex, ParseConflict.EXPECTED_INTEGER);

        result.add(new IntegerToken(argumentIndex, numericArgument));
        continue;
      }

      // Ensure that there are no quotes wedged into search-terms
      // At this point, first and last char have been checked against, so skip them
      for (var argIndex = 1; argIndex < argLength - 1; ++argIndex) {
        if (arg.charAt(argIndex) == '"')
          throw new ArgumentParseException(argumentIndex, ParseConflict.MALFORMED_STRING_ARGUMENT);
      }

      result.add(new UnquotedStringToken(argumentIndex, arg));
    }

    if (stringBeginArgumentIndex != -1)
      throw new ArgumentParseException(stringBeginArgumentIndex, ParseConflict.MISSING_STRING_TERMINATION);

    return result;
  }
}