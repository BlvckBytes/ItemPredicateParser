package me.blvckbytes.item_predicate_parser.token;

import org.jetbrains.annotations.Nullable;

/**
 * @param value The null-value represents a wildcard
 */
public record IntegerToken(
  int commandArgumentIndex,
  int firstCharIndex,
  @Nullable Integer value,
  boolean wasTimeNotation,
  ComparisonMode comparisonMode
) implements Token {

  public IntegerToken(int commandArgumentIndex, int firstCharIndex, @Nullable Integer value) {
    this(commandArgumentIndex, firstCharIndex, value, false, ComparisonMode.EQUALS);
  }

  public boolean matches(@Nullable Integer value) {
    if (this.value == null)
      return true;

    if (value == null)
      return false;

    return switch (comparisonMode) {
      case EQUALS -> this.value.equals(value);
      case GREATER_THAN -> this.value < value;
      case LESS_THAN -> this.value > value;
    };
  }

  public String stringify() {
    if (this.value == null)
      return "*";

    if (this.comparisonMode == ComparisonMode.GREATER_THAN)
      return ">" + this.value;

    if (this.comparisonMode == ComparisonMode.LESS_THAN)
      return "<" + this.value;

    return this.value.toString();
  }
}
