package me.blvckbytes.item_predicate_parser.token;

public record UnquotedStringToken(int commandArgumentIndex, int firstCharIndex, String value) implements Token {

  @Override
  public String stringify() {
    return escapeDoubleQuotes(value);
  }

  public static String escapeDoubleQuotes(String input) {
    // One additional escape-char per double-quote; 8 should cover most cases to avoid re-allocation
    var result = new StringBuilder(input.length() + 8);

    for (var i = 0; i < input.length(); ++i) {
      var c = input.charAt(i);

      if (c == '"')
        result.append('\\');

      result.append(c);
    }

    return result.toString();
  }
}
