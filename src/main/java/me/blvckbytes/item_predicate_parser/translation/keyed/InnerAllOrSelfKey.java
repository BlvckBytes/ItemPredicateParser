package me.blvckbytes.item_predicate_parser.translation.keyed;

import me.blvckbytes.item_predicate_parser.translation.TranslationLanguage;
import org.jetbrains.annotations.Nullable;

public class InnerAllOrSelfKey implements LangKeyed<InnerAllOrSelfKey> {

  public static final InnerAllOrSelfKey INSTANCE = new InnerAllOrSelfKey();

  private InnerAllOrSelfKey() {}

  @Override
  public String getLanguageFileKey() {
    throw new UnsupportedOperationException();
  }

  @Override
  public @Nullable String resolveTranslationDirectly(TranslationLanguage language) {
    return language.customTranslations.innerAllOrSelf();
  }

  @Override
  public InnerAllOrSelfKey getWrapped() {
    return this;
  }

  @Override
  public LangKeyedPredicateType getPredicateType() {
    return LangKeyedPredicateType.NOT_A_PREDICATE;
  }
}
