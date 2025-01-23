package me.blvckbytes.item_predicate_parser;

import org.jetbrains.annotations.Nullable;

public interface SingletonTranslationRegistry {

  @Nullable String getTranslationBySingleton(Object instance);

}
