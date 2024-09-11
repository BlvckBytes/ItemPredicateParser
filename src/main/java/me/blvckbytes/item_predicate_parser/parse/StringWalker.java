package me.blvckbytes.item_predicate_parser.parse;

public class StringWalker {

  private final String string;
  private int nextCharIndex = 0;
  private int spaceCounter = 0;
  private int charsSinceLastSpace = 0;

  public StringWalker(String string) {
    this.string = string;
  }

  public static StringWalker fromArgumentArray(String[] args, int offset) {
    /*
      NOTE: I have attempted to write a walker which directly operates on the string-array
            using two indices (current-part and current-char), but the required logic to do
            so including peeking next and previous, retrieving next and undo next, as well
            as properly emitting "virtual spaces" between arguments was blowing out of
            proportion rather quickly. While the little state-machine technically worked, it's
            very laborious to test through-and-through and would add quite the mental burden.
     */

    // 256 is the max minecraft chat message length - probably a good initial capacity to avoid re-alloc
    var result = new StringBuilder(256);

    for (var partIndex = offset; partIndex < args.length; ++partIndex) {
      if (partIndex != offset)
        result.append(' ');

      result.append(args[partIndex]);
    }

    var walker = new StringWalker(result.toString());
    walker.spaceCounter += offset;
    return walker;
  }

  private char getChar(int index) {
    return index >= string.length() ? 0 : string.charAt(index);
  }

  public char peekChar() {
    return getChar(nextCharIndex);
  }

  public char nextChar() {
    var result = getChar(nextCharIndex++);

    if (result == ' ') {
      charsSinceLastSpace = 0;
      ++spaceCounter;
    }
    else
      ++charsSinceLastSpace;

    return result;
  }

  public void undoNextChar() {
    if (nextCharIndex == 0)
      return;

    --nextCharIndex;

    if (getChar(nextCharIndex) == ' ') {
      charsSinceLastSpace = 0;
      return;
    }

    --charsSinceLastSpace;
  }

  public char peekPreviousChar() {
    if (nextCharIndex == 0)
      return 0;

    return getChar(nextCharIndex - 1);
  }

  public char charAt(int charIndex) {
    if (charIndex < 0 || charIndex >= string.length())
      return 0;

    return string.charAt(charIndex);
  }

  public boolean isConsideredWhitespace(char c) {
    return c == ' ' || c == '\t' || c == '\n' || c == '\r';
  }

  public void consumeWhitespace() {
    while (isConsideredWhitespace(peekChar()))
      nextChar();
  }

  public int getNextCharIndex() {
    return nextCharIndex;
  }

  public int getCharsSinceLastSpace() {
    return charsSinceLastSpace;
  }

  public String makeSubstring(int beginCharIndex) {
    return string.substring(beginCharIndex, nextCharIndex);
  }

  public int getArgumentIndex() {
    return spaceCounter;
  }
}
