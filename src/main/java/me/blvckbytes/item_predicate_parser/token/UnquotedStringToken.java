package me.blvckbytes.item_predicate_parser.token;

import me.blvckbytes.item_predicate_parser.parse.ParserInput;

public record UnquotedStringToken(
  int commandArgumentIndex,
  int firstCharIndex,
  ParserInput parserInput,
  String value
) implements Token {

  @Override
  public String stringify() {
    return value;
  }
}
