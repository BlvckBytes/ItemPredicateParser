package me.blvckbytes.storage_query.token;

public record QuotedStringToken(int commandArgumentIndex, String value) implements Token {

  @Override
  public String stringify() {
    return "\"" + value + "\"";
  }
}
