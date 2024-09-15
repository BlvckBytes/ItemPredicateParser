package me.blvckbytes.item_predicate_parser.token;

import me.blvckbytes.item_predicate_parser.parse.ParserInput;

public record QuotedStringToken(
  int beginCommandArgumentIndex,
  int beginFirstCharIndex,
  int endCommandArgumentIndex,
  int endLastCharIndex,
  ParserInput parserInput,
  String value
) implements Token {

  @Override
  public String stringify() {
    return "\"" + value.replace("\"", "\\\"") + "\"";
  }
}
