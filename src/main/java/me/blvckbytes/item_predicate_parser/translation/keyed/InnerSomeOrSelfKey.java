package me.blvckbytes.item_predicate_parser.translation.keyed;

import me.blvckbytes.item_predicate_parser.translation.TranslationLanguage;
import org.jetbrains.annotations.Nullable;

public class InnerSomeOrSelfKey implements LangKeyed<InnerSomeOrSelfKey> {

  public static final InnerSomeOrSelfKey INSTANCE = new InnerSomeOrSelfKey();

  private InnerSomeOrSelfKey() {}

  @Override
  public String getLanguageFileKey() {
    throw new UnsupportedOperationException();
  }

  @Override
  public @Nullable String resolveTranslationDirectly(TranslationLanguage language) {
    return language.customTranslations.innerSomeOrSelf();
  }

  @Override
  public InnerSomeOrSelfKey getWrapped() {
    return this;
  }

  @Override
  public LangKeyedPredicateType getPredicateType() {
    return LangKeyedPredicateType.NOT_A_PREDICATE;
  }
}
