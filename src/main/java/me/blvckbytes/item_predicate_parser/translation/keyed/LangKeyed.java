package me.blvckbytes.item_predicate_parser.translation.keyed;

import me.blvckbytes.item_predicate_parser.translation.TranslationLanguage;
import org.jetbrains.annotations.Nullable;

public interface LangKeyed<T> {

  String getLanguageFileKey();

  default @Nullable String resolveTranslationDirectly(TranslationLanguage language) {
    return null;
  }

  T getWrapped();

  LangKeyedPredicateType getPredicateType();

}
