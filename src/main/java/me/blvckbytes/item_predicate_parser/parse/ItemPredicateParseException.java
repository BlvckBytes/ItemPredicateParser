package me.blvckbytes.item_predicate_parser.parse;

import me.blvckbytes.item_predicate_parser.token.Token;

import java.util.StringJoiner;

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
    var markedExpression = new StringJoiner(" ");

    var input = token.parserInput();
    var args = input.getInputAsArguments();

    for (var argIndex = input.getArgumentsOffset(); argIndex < args.length; ++argIndex) {
      var arg = args[argIndex];

      if (token.beginCommandArgumentIndex() != argIndex) {
        markedExpression.add(nonHighlightPrefix + arg);
        continue;
      }

      if (token.beginFirstCharIndex() == 0) {
        markedExpression.add(highlightPrefix + arg);
        continue;
      }

      markedExpression.add(
        nonHighlightPrefix + arg.substring(0, token.beginFirstCharIndex()) +
        highlightPrefix + arg.substring(token.beginFirstCharIndex())
      );
    }

    return markedExpression.toString();
  }
}
