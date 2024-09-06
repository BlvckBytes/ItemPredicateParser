package me.blvckbytes.item_predicate_parser.translation;

import org.jetbrains.annotations.Nullable;

public interface ILanguageRegistry {
  @Nullable TranslationRegistry getTranslationRegistry(TranslationLanguage language);
}
