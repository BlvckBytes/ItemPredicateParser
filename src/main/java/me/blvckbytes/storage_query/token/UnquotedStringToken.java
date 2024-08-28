package me.blvckbytes.storage_query.token;

public record UnquotedStringToken(int commandArgumentIndex, String value) implements Token {

  @Override
  public String stringify() {
    return value;
  }
}
