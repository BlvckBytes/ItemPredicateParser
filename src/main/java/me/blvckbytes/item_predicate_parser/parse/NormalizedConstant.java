package me.blvckbytes.item_predicate_parser.parse;

import java.util.ArrayList;
import java.util.List;

public class NormalizedConstant<T extends Enum<?>> {

  public final T constant;
  public final String normalizedName;
  private final List<SubstringIndices> normalizedNameIndices;

  public NormalizedConstant(T constant) {
    this.constant = constant;
    this.normalizedName = normalizeName(constant.name());
    this.normalizedNameIndices = SubstringIndices.forString(null, this.normalizedName, '-');
  }

  public ArrayList<SubstringIndices> getNormalizedNameIndices() {
    return new ArrayList<>(normalizedNameIndices);
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
