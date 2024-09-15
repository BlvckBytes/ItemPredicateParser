package me.blvckbytes.item_predicate_parser.token;

import me.blvckbytes.item_predicate_parser.parse.ParserInput;

public record UnquotedStringToken(
  int beginCommandArgumentIndex,
  int beginFirstCharIndex,
  ParserInput parserInput,
  String value
) implements Token {

  @Override
  public int endCommandArgumentIndex() {
    return beginCommandArgumentIndex;
  }

  @Override
  public int endLastCharIndex() {
    return beginFirstCharIndex + (value.length() - 1);
  }

  @Override
  public String stringify() {
    return value;
  }
}
