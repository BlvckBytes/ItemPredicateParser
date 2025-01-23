package me.blvckbytes.item_predicate_parser;

import me.blvckbytes.item_predicate_parser.translation.TranslationLanguage;
import org.jetbrains.annotations.NotNull;

public interface TranslationLanguageRegistry {

  @NotNull SingletonTranslationRegistry getTranslationRegistry(TranslationLanguage language);

}
