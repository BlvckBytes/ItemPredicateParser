package me.blvckbytes.item_predicate_parser;

import me.blvckbytes.item_predicate_parser.translation.TranslatedLangKeyed;
import me.blvckbytes.item_predicate_parser.translation.TranslationRegistry;
import me.blvckbytes.item_predicate_parser.translation.keyed.LangKeyed;

public record TranslatedLangKeyedPredicate(
  TranslationRegistry translationRegistry,
  LangKeyed<?> langKeyed,
  String collisionPrefix
) {
  public boolean test(TranslatedLangKeyed item) {
    if (!item.langKeyed.equals(langKeyed))
      return false;

    var unPrefixedTranslation = item.normalizedUnPrefixedTranslation;
    var normalizedPrefixed = TranslatedLangKeyed.normalize(collisionPrefix + unPrefixedTranslation);

    return item.normalizedPrefixedTranslation.equals(normalizedPrefixed);
  }
}
