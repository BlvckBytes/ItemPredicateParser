package me.blvckbytes.item_predicate_parser.parse;

public class ItemPredicateParseException extends RuntimeException {

  private final int argumentIndex;
  private final int firstCharIndex;
  private final ParseConflict conflict;

  public ItemPredicateParseException(int argumentIndex, int firstCharIndex, ParseConflict conflict) {
    this.argumentIndex = argumentIndex;
    this.firstCharIndex = firstCharIndex;
    this.conflict = conflict;
  }

  public int getArgumentIndex() {
    return argumentIndex;
  }

  public int getFirstCharIndex() {
    return firstCharIndex;
  }

  public ParseConflict getConflict() {
    return conflict;
  }
}
