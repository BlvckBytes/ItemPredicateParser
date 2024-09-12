package me.blvckbytes.item_predicate_parser.parse;

import me.blvckbytes.item_predicate_parser.token.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class TokenParser {

  private static final char INTEGER_WILDCARD_CHAR = '*';

  public static ArrayList<Token> parseTokens(String text) {
    return parseTokens(new StringWalker(text));
  }

  public static ArrayList<Token> parseTokens(String[] args, int offset) {
    return parseTokens(StringWalker.fromArgumentArray(args, offset));
  }

  private static ArrayList<Token> parseTokens(StringWalker walker) {
    var result = new ArrayList<Token>();

    Token token;
    char c;

    while (true) {
      walker.consumeWhitespace();

      if ((c = walker.peekChar()) == 0)
        break;

      if (c == '(' || c == ')') {
        result.add(new ParenthesisToken(walker.getArgumentIndex(), walker.getCharsSinceLastSpace(), walker, c == '('));
        walker.nextChar();
        continue;
      }

      if ((token = tryConsumeQuotedString(walker)) != null) {
        result.add(token);
        continue;
      }

      if ((token = tryConsumeInteger(walker)) != null) {
        result.add(token);
        continue;
      }

      if ((token = tryConsumeUnquotedString(walker)) != null)
        result.add(token);
    }

    return result;
  }

  private static @Nullable QuotedStringToken tryConsumeQuotedString(StringWalker walker) {
    if (walker.peekChar() != '"')
      return null;

    int beginArgumentIndex = walker.getArgumentIndex();
    int firstCharIndex = walker.getCharsSinceLastSpace();

    walker.nextChar();

    char c;

    var stringContents = new StringBuilder(64);

    while ((c = walker.peekChar()) != 0) {
      if (c == '"') {
        // Allow to escape double-quotes within strings
        if (walker.peekPreviousChar() == '\\') {
          stringContents.setCharAt(stringContents.length() - 1, walker.nextChar());
          continue;
        }

        break;
      }

      stringContents.append(walker.nextChar());
    }

    if (walker.nextChar() != '"')
      throw new ItemPredicateParseException(beginArgumentIndex, firstCharIndex, ParseConflict.MISSING_STRING_TERMINATION);

    var stringValue = stringContents.toString();

    if (stringValue.isBlank())
      throw new ItemPredicateParseException(beginArgumentIndex, firstCharIndex, ParseConflict.EMPTY_OR_BLANK_QUOTED_STRING);

    return new QuotedStringToken(beginArgumentIndex, firstCharIndex, walker, stringValue);
  }

  private static @Nullable IntegerToken tryConsumeInteger(StringWalker walker) {
    char firstChar = walker.peekChar();
    var firstCharIndex = walker.getCharsSinceLastSpace();

    if (firstChar == INTEGER_WILDCARD_CHAR) {
      walker.nextChar();
      return new IntegerToken(walker.getArgumentIndex(), firstCharIndex, walker, null, false, ComparisonMode.EQUALS);
    }

    var comparisonMode = ComparisonMode.EQUALS;

    if (firstChar == '>' || firstChar == '<') {
      comparisonMode = firstChar == '>' ? ComparisonMode.GREATER_THAN : ComparisonMode.LESS_THAN;
      walker.nextChar();
    }

    // Not a number, but rather an unquoted string
    if (!Character.isDigit(walker.peekChar())) {
      if (comparisonMode != ComparisonMode.EQUALS)
        walker.undoNextChar();

      return null;
    }

    var blocks = new int[] { -1, -1, -1 };
    var blocksIndex = 0;

    var blockBeginIndex = walker.getNextCharIndex();
    char currentChar;

    while (true) {
      var currentCharIndex = walker.getNextCharIndex();
      currentChar = walker.peekChar();

      var isEnd = currentChar == 0 || currentChar == ')' || walker.isConsideredWhitespace(currentChar);

      if (!isEnd)
        walker.nextChar();

      if (currentChar == ':' || isEnd) {
        int currentBlockValue = 0;
        int digitPlaceValue = 0;

        for (int index = currentCharIndex - 1; index >= blockBeginIndex; --index)
          currentBlockValue += (walker.charAt(index) - '0') * (int) Math.pow(10, digitPlaceValue++);

        if (blocksIndex == blocks.length)
          throw new ItemPredicateParseException(walker.getArgumentIndex(), firstCharIndex, ParseConflict.TOO_MANY_TIME_NOTATION_BLOCKS);

        blocks[blocksIndex++] = currentBlockValue;

        if (isEnd)
          break;

        blockBeginIndex = currentCharIndex + 1;
        continue;
      }

      if (!(currentChar >= '0' && currentChar <= '9'))
        throw new ItemPredicateParseException(walker.getArgumentIndex(), firstCharIndex, ParseConflict.EXPECTED_CORRECT_INTEGER);
    }

    var blockPower = 0;
    var resultingNumber = 0;

    for (var blockIndex = blocks.length - 1; blockIndex >= 0; --blockIndex) {
      if (blocks[blockIndex] < 0)
        continue;

      resultingNumber += blocks[blockIndex] * (int) Math.pow(60, blockPower++);
    }

    return new IntegerToken(walker.getArgumentIndex(), firstCharIndex, walker, resultingNumber, blockPower > 1, comparisonMode);
  }

  private static @Nullable UnquotedStringToken tryConsumeUnquotedString(StringWalker walker) {
    var beginIndex = walker.getNextCharIndex();
    var firstCharIndex = walker.getCharsSinceLastSpace();

    char c;

    while ((c = walker.peekChar()) != 0) {
      // Trailing token-chars are interpreted as such, by definition
      if (walker.isConsideredWhitespace(c) || c == ')' || c == '(' || c == '"')
        break;

      walker.nextChar();
    }

    if (walker.getNextCharIndex() == beginIndex)
      return null;

    return new UnquotedStringToken(walker.getArgumentIndex(), firstCharIndex, walker, walker.makeSubstring(beginIndex));
  }
}