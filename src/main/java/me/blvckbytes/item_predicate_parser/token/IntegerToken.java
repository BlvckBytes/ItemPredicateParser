package me.blvckbytes.item_predicate_parser.token;

import me.blvckbytes.item_predicate_parser.parse.ParserInput;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * @param value The null-value represents a wildcard
 */
public record IntegerToken(
  int beginCommandArgumentIndex,
  int beginFirstCharIndex,
  int endLastCharIndex,
  ParserInput parserInput,
  @Nullable Integer value,
  boolean wasTimeNotation,
  ComparisonMode comparisonMode
) implements Token {

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

  @Override
  public int endCommandArgumentIndex() {
    return beginCommandArgumentIndex;
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

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof IntegerToken otherToken))
      return false;

    if (this.wasTimeNotation != otherToken.wasTimeNotation)
      return false;

    if (this.comparisonMode != otherToken.comparisonMode)
      return false;

    return Objects.equals(this.value, otherToken.value);
  }
}
