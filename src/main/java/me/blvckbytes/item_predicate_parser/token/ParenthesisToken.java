package me.blvckbytes.item_predicate_parser.token;

import me.blvckbytes.item_predicate_parser.parse.ParserInput;

public record ParenthesisToken (
  int commandArgumentIndex,
  int firstCharIndex,
  ParserInput parserInput,
  boolean isOpening
) implements Token {

  @Override
  public String stringify() {
    return isOpening ? "(" : ")";
  }
}
