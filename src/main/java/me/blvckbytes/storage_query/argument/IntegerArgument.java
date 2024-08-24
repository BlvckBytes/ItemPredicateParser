package me.blvckbytes.storage_query.argument;

import org.jetbrains.annotations.Nullable;

/**
 * @param value The null-value represents a wildcard
 */
public record IntegerArgument(
  int commandArgumentIndex,
  @Nullable Integer value
) implements Argument {

  @Override
  public int getCommandArgumentIndex() {
    return commandArgumentIndex;
  }

  public boolean matches(@Nullable Integer value) {
    if (this.value == null)
      return true;
    return this.value.equals(value);
  }

  public String stringify() {
    if (this.value == null)
      return "*";
    return this.value.toString();
  }
}
