package me.blvckbytes.item_predicate_parser;

import me.blvckbytes.item_predicate_parser.translation.TranslatedTranslatable;
import me.blvckbytes.item_predicate_parser.translation.TranslationRegistry;
import org.bukkit.Translatable;

public record TranslatedTranslatablePredicate(
  TranslationRegistry translationRegistry,
  Translatable translatable,
  String collisionPrefix
) {
  public boolean test(TranslatedTranslatable item) {
    if (item.translatable != translatable)
      return false;

    var vanillaTranslation = translationRegistry.getTranslationOrNull(item.translatable);
    var normalizedPrefixed = TranslatedTranslatable.normalize(collisionPrefix + vanillaTranslation);

    return item.normalizedPrefixedTranslation.equals(normalizedPrefixed);
  }
}
