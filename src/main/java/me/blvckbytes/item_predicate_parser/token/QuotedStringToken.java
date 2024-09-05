package me.blvckbytes.item_predicate_parser.token;

public record QuotedStringToken(int commandArgumentIndex, int firstCharIndex, String value) implements Token {

  @Override
  public String stringify() {
    return "\"" + value.replace("\"", "\\\"") + "\"";
  }
}
