package me.blvckbytes.item_predicate_parser.parse;

public class NormalizedConstant<T extends Enum<?>> {

  public final T constant;
  public final String normalizedName;
  public final Syllables syllables;

  public NormalizedConstant(T constant) {
    this.constant = constant;
    this.normalizedName = normalizeName(constant.name());
    this.syllables = Syllables.forString(null, this.normalizedName, Syllables.DELIMITER_SEARCH_PATTERN);
  }

  private static String normalizeName(String name) {
    var result = new StringBuilder();
    char previousChar = 0;

    for (var charIndex = 0; charIndex < name.length(); ++charIndex) {
      var currentChar = name.charAt(charIndex);

      if (currentChar == '_')
        result.append('-');
      else if (charIndex == 0 || previousChar == '-' || previousChar == '_')
        result.append(Character.toUpperCase(currentChar));
      else
        result.append(Character.toLowerCase(currentChar));

      previousChar = currentChar;
    }

    return result.toString();
  }
}
