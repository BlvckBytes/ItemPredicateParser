package me.blvckbytes.item_predicate_parser.token;

public record UnquotedStringToken(int commandArgumentIndex, String value) implements Token {

  @Override
  public String stringify() {
    return value;
  }
}
