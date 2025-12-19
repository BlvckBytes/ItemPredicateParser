package me.blvckbytes.item_predicate_parser.translation.keyed;

import me.blvckbytes.item_predicate_parser.translation.TranslationLanguage;
import org.jetbrains.annotations.Nullable;

public class AnyKey implements LangKeyed<AnyKey> {

  public static final AnyKey INSTANCE = new AnyKey();

  private AnyKey() {}

  @Override
  public String getLanguageFileKey() {
    throw new UnsupportedOperationException();
  }

  @Override
  public @Nullable String resolveTranslationDirectly(TranslationLanguage language) {
    return language.customTranslations.any();
  }

  @Override
  public AnyKey getWrapped() {
    return this;
  }

  @Override
  public LangKeyedPredicateType getPredicateType() {
    return LangKeyedPredicateType.ANY;
  }
}
