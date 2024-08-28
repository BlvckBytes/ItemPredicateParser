package me.blvckbytes.storage_query.token;

public record ParenthesisToken (int commandArgumentIndex, boolean isOpening) implements Token {

  @Override
  public String stringify() {
    return isOpening ? "(" : ")";
  }
}
