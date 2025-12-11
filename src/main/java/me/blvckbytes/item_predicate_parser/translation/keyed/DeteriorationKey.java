package me.blvckbytes.item_predicate_parser.translation.keyed;

import me.blvckbytes.item_predicate_parser.translation.TranslationLanguage;
import org.jetbrains.annotations.Nullable;

public class DeteriorationKey implements LangKeyed<DeteriorationKey> {

  public static final DeteriorationKey INSTANCE = new DeteriorationKey();

  private DeteriorationKey() {}

  @Override
  public String getLanguageFileKey() {
    throw new UnsupportedOperationException();
  }

  @Override
  public @Nullable String resolveTranslationDirectly(TranslationLanguage language) {
    return language.customTranslations.deterioration();
  }

  @Override
  public DeteriorationKey getWrapped() {
    return this;
  }

  @Override
  public LangKeyedPredicateType getPredicateType() {
    return LangKeyedPredicateType.DETERIORATION;
  }
}
