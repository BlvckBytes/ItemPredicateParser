package me.blvckbytes.item_predicate_parser.token;

import me.blvckbytes.item_predicate_parser.parse.ParserInput;

public record ParenthesisToken (
  int beginCommandArgumentIndex,
  int beginFirstCharIndex,
  ParserInput parserInput,
  boolean isOpening
) implements Token {

  @Override
  public int endCommandArgumentIndex() {
    return beginCommandArgumentIndex;
  }

  @Override
  public int endLastCharIndex() {
    return beginFirstCharIndex;
  }

  @Override
  public String stringify() {
    return isOpening ? "(" : ")";
  }
}
