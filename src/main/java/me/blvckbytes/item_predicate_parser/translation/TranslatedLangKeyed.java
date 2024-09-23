package me.blvckbytes.item_predicate_parser.translation;

import me.blvckbytes.item_predicate_parser.parse.SubstringIndices;
import me.blvckbytes.item_predicate_parser.translation.keyed.LangKeyed;

import java.util.ArrayList;
import java.util.List;

public class TranslatedLangKeyed {

  public final LangKeyedSource source;
  public final LangKeyed<?> langKeyed;
  public final String normalizedUnPrefixedTranslation;
  public final String normalizedPrefixedTranslation;
  private final List<SubstringIndices> partIndices;

  public int alphabeticalIndex = 0;

  public TranslatedLangKeyed(
    LangKeyedSource source,
    LangKeyed<?> langKeyed,
    String normalizedUnPrefixedTranslation,
    String normalizedPrefixedTranslation
  ) {
    this.source = source;
    this.langKeyed = langKeyed;
    this.normalizedUnPrefixedTranslation = normalizedUnPrefixedTranslation;
    this.normalizedPrefixedTranslation = normalizedPrefixedTranslation;
    this.partIndices = SubstringIndices.forString(null, normalizedPrefixedTranslation, SubstringIndices.SEARCH_PATTERN_DELIMITER);
  }

  public ArrayList<SubstringIndices> getPartIndicesCopy() {
    return new ArrayList<>(partIndices);
  }

  @Override
  public String toString() {
    return normalizedPrefixedTranslation;
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
