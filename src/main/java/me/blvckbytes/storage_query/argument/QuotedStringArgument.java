package me.blvckbytes.storage_query.argument;

public record QuotedStringArgument(int commandArgumentIndex, String value) implements Argument {

  @Override
  public int getCommandArgumentIndex() {
    return commandArgumentIndex;
  }
}
