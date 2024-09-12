package me.blvckbytes.item_predicate_parser;

import me.blvckbytes.item_predicate_parser.token.*;
import org.jetbrains.annotations.Nullable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public abstract class MakeAndCompareTokens {

  protected IntegerToken integerToken(int commandArgumentIndex, int firstCharIndex, @Nullable Integer value) {
    return integerToken(commandArgumentIndex, firstCharIndex, value, false, ComparisonMode.EQUALS);
  }

  protected IntegerToken integerToken(
    int commandArgumentIndex,
    int firstCharIndex,
    @Nullable Integer value,
    boolean wasTimeNotation,
    ComparisonMode comparisonMode
  ) {
    return new IntegerToken(commandArgumentIndex, firstCharIndex, null, value, wasTimeNotation, comparisonMode);
  }

  protected ParenthesisToken parenthesisToken(int commandArgumentIndex, int firstCharIndex, boolean isOpening) {
    return new ParenthesisToken(commandArgumentIndex, firstCharIndex, null, isOpening);
  }

  protected QuotedStringToken quotedStringToken(int commandArgumentIndex, int firstCharIndex, String value) {
    return new QuotedStringToken(commandArgumentIndex, firstCharIndex, null, value);
  }

  protected UnquotedStringToken unquotedStringToken(int commandArgumentIndex, int firstCharIndex, String value) {
    return new UnquotedStringToken(commandArgumentIndex, firstCharIndex, null, value);
  }

  protected void compareTokens(@Nullable Token expected, @Nullable Token actual) {
    if (expected == null && actual == null)
      return;

    assertNotNull(expected);
    assertNotNull(actual);

    assertEquals(expected.getClass(), actual.getClass(), "Tokens not comparable - expected same type");

    // TODO: This is an unacceptable solution; only ignored fields should be handled manually, as this may easily
    //       lead to new properties being not accounted for in the tests, if they are forgotten to be added.

    assertEquals(expected.commandArgumentIndex(), actual.commandArgumentIndex());
    assertEquals(expected.firstCharIndex(), actual.firstCharIndex());
    // Do not compare the input, as that will always be null when testing

    if ((expected instanceof IntegerToken expectedInt) && (actual instanceof IntegerToken actualInt)) {
      assertEquals(expectedInt.value(), actualInt.value());
      assertEquals(expectedInt.wasTimeNotation(), actualInt.wasTimeNotation());
      assertEquals(expectedInt.comparisonMode(), actualInt.comparisonMode());
      return;
    }

    if ((expected instanceof QuotedStringToken expectedString) && (actual instanceof QuotedStringToken actualString)) {
      assertEquals(expectedString.value(), actualString.value());
      return;
    }

    if ((expected instanceof UnquotedStringToken expectedString) && (actual instanceof UnquotedStringToken actualString)) {
      assertEquals(expectedString.value(), actualString.value());
      return;
    }

    if ((expected instanceof ParenthesisToken expectedParen) && (actual instanceof ParenthesisToken actualParen)) {
      assertEquals(expectedParen.isOpening(), actualParen.isOpening());
      return;
    }

    throw new IllegalStateException("Do not know how to compare tokens of type " + expected.getClass());
  }
}
