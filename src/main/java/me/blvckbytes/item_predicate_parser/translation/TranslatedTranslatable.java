package me.blvckbytes.item_predicate_parser.translation;

import me.blvckbytes.item_predicate_parser.parse.SubstringIndices;
import org.bukkit.Translatable;

import java.util.List;

public class TranslatedTranslatable {

  public final TranslatableSource source;
  public final Translatable translatable;
  public final String normalizedTranslation;
  public final List<SubstringIndices> partIndices;

  public int alphabeticalIndex = 0;

  public TranslatedTranslatable(
    TranslatableSource source,
    Translatable translatable,
    String normalizedTranslation
  ) {
    this.source = source;
    this.translatable = translatable;
    this.normalizedTranslation = normalizedTranslation;
    this.partIndices = SubstringIndices.forString(null, normalizedTranslation, SubstringIndices.SEARCH_PATTERN_DELIMITER);
  }

  @Override
  public String toString() {
    return normalizedTranslation;
  }

  public static String normalize(String input) {
    var result = input.toCharArray();

    for (var i = 0; i < result.length; ++i) {
      var c = result[i];

      var newChar = switch (c) {
        // Avoids ambiguity in relation to quoted strings
        case '"' -> '\'';
        // Avoids ambiguity in relation to logical groups
        case '(' -> '[';
        case ')' -> ']';
        // Syllable-patterns are separated by spaces
        case ' ' -> '-';
        // Just for uniformity
        case '_' -> '-';
        default -> c;
      };

      if (newChar != c)
        result[i] = newChar;
    }

    return new String(result);
  }
}
