package me.blvckbytes.item_predicate_parser.translation.keyed;

import me.blvckbytes.item_predicate_parser.translation.TranslationLanguage;
import org.jetbrains.annotations.Nullable;

public class InnerSomeKey implements LangKeyed<InnerSomeKey> {

  public static final InnerSomeKey INSTANCE = new InnerSomeKey();

  private InnerSomeKey() {}

  @Override
  public String getLanguageFileKey() {
    throw new UnsupportedOperationException();
  }

  @Override
  public @Nullable String resolveTranslationDirectly(TranslationLanguage language) {
    return language.customTranslations.innerSome();
  }

  @Override
  public InnerSomeKey getWrapped() {
    return this;
  }

  @Override
  public LangKeyedPredicateType getPredicateType() {
    return LangKeyedPredicateType.NOT_A_PREDICATE;
  }
}
