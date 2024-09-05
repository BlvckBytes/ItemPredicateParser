package me.blvckbytes.item_predicate_parser.token;

public record ParenthesisToken (int commandArgumentIndex, int firstCharIndex, boolean isOpening) implements Token {

  @Override
  public String stringify() {
    return isOpening ? "(" : ")";
  }
}
