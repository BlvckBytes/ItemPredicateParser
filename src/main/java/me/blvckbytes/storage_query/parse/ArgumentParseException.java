package me.blvckbytes.storage_query.parse;

public class ArgumentParseException extends RuntimeException {

  private final int argumentIndex;
  private final ParseConflict conflict;

  public ArgumentParseException(int argumentIndex, ParseConflict conflict) {
    this.argumentIndex = argumentIndex;
    this.conflict = conflict;
  }

  public int getArgumentIndex() {
    return argumentIndex;
  }

  public ParseConflict getConflict() {
    return conflict;
  }
}
