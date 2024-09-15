package me.blvckbytes.item_predicate_parser.parse;

import me.blvckbytes.item_predicate_parser.token.Token;

public class ItemPredicateParseException extends RuntimeException {

  private final Token token;
  private final ParseConflict conflict;

  public ItemPredicateParseException(Token token, ParseConflict conflict) {
    this.token = token;
    this.conflict = conflict;
  }

  public Token getToken() {
    return token;
  }

  public ParseConflict getConflict() {
    return conflict;
  }

  public String highlightedInput(String nonHighlightPrefix, String highlightPrefix) {
    String lastAppendedPrefix = null;
    var result = new StringBuilder(256);

    var input = token.parserInput();
    var args = input.getInputAsArguments();

    /*
                            begin                        end
      argumentIndex         0                            5
      charIndex             8                            6
                    unquoted"hello world this is a string"another
     */

    // This is a bit verbose... but maybe I shouldn't try to be too clever about it.

    for (var argIndex = input.getArgumentsOffset(); argIndex < args.length; ++argIndex) {
      var arg = args[argIndex];

      if (!result.isEmpty())
        result.append(' ');

      if (argIndex < token.beginCommandArgumentIndex() || argIndex > token.endCommandArgumentIndex()) {
        if (!nonHighlightPrefix.equals(lastAppendedPrefix))
          result.append(nonHighlightPrefix);

        result.append(arg);

        lastAppendedPrefix = nonHighlightPrefix;
        continue;
      }

      if (argIndex == token.beginCommandArgumentIndex()) {
        if (argIndex == token.endCommandArgumentIndex()) {
          if (arg.length() == token.endLastCharIndex() + 1) {
            if (token.beginFirstCharIndex() != 0 && !nonHighlightPrefix.equals(lastAppendedPrefix))
              result.append(nonHighlightPrefix);

            result.append(arg, 0, token.beginFirstCharIndex());
            result.append(highlightPrefix);
            result.append(arg.substring(token.beginFirstCharIndex()));

            lastAppendedPrefix = highlightPrefix;
          } else {
            if (token.beginFirstCharIndex() != 0 && !nonHighlightPrefix.equals(lastAppendedPrefix))
              result.append(nonHighlightPrefix);

            result.append(arg, 0, token.beginFirstCharIndex());
            result.append(highlightPrefix);
            result.append(arg, token.beginFirstCharIndex(), token.endLastCharIndex() + 1);
            result.append(nonHighlightPrefix);
            result.append(arg.substring(token.endLastCharIndex() + 1));

            lastAppendedPrefix = nonHighlightPrefix;
          }
          continue;
        }

        if (token.beginFirstCharIndex() != 0 && !nonHighlightPrefix.equals(lastAppendedPrefix))
          result.append(nonHighlightPrefix);

        result.append(arg, 0, token.beginFirstCharIndex());
        result.append(highlightPrefix);
        result.append(arg.substring(token.beginFirstCharIndex()));

        lastAppendedPrefix = highlightPrefix;
        continue;
      }

      if (argIndex == token.endCommandArgumentIndex()) {
        if (arg.length() == token.endLastCharIndex() + 1) {
          if (!highlightPrefix.equals(lastAppendedPrefix))
            result.append(highlightPrefix);

          result.append(arg);

          lastAppendedPrefix = highlightPrefix;
        } else {
          if (!highlightPrefix.equals(lastAppendedPrefix))
            result.append(highlightPrefix);

          result.append(arg, 0, token.endLastCharIndex() + 1);
          result.append(nonHighlightPrefix);
          result.append(arg.substring(token.endLastCharIndex() + 1));

          lastAppendedPrefix = nonHighlightPrefix;
        }

        continue;
      }

      if (!highlightPrefix.equals(lastAppendedPrefix))
        result.append(highlightPrefix);

      result.append(arg);

      lastAppendedPrefix = highlightPrefix;
    }

    return result.toString();
  }
}
