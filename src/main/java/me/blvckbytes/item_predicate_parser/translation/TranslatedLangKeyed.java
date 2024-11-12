package me.blvckbytes.item_predicate_parser.translation;

import me.blvckbytes.item_predicate_parser.translation.keyed.LangKeyed;
import me.blvckbytes.syllables_matcher.Syllables;

public class TranslatedLangKeyed<T extends LangKeyed<?>> {

  public final LangKeyedSource source;
  public final T langKeyed;
  public final String normalizedUnPrefixedTranslation;
  public final String normalizedPrefixedTranslation;
  public final Syllables syllables;

  public int alphabeticalIndex = 0;

  public TranslatedLangKeyed(
    LangKeyedSource source,
    T langKeyed,
    String normalizedUnPrefixedTranslation,
    String normalizedPrefixedTranslation
  ) {
    this.source = source;
    this.langKeyed = langKeyed;
    this.normalizedUnPrefixedTranslation = normalizedUnPrefixedTranslation;
    this.normalizedPrefixedTranslation = normalizedPrefixedTranslation;
    this.syllables = Syllables.forString(normalizedPrefixedTranslation, Syllables.DELIMITER_SEARCH_PATTERN);
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
