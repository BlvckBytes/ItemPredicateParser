package me.blvckbytes.storage_query.token;

import org.jetbrains.annotations.Nullable;

/**
 * @param value The null-value represents a wildcard
 */
public record IntegerToken(
  int commandArgumentIndex,
  @Nullable Integer value,
  boolean wasTimeNotation
) implements Token {

  public IntegerToken(int commandArgumentIndex, @Nullable Integer value) {
    this(commandArgumentIndex, value, false);
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
