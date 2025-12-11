package me.blvckbytes.item_predicate_parser.translation.keyed;

import me.blvckbytes.item_predicate_parser.translation.TranslationLanguage;
import org.jetbrains.annotations.Nullable;

public class AmountKey implements LangKeyed<AmountKey> {

  public static final AmountKey INSTANCE = new AmountKey();

  private AmountKey() {}

  @Override
  public String getLanguageFileKey() {
    throw new UnsupportedOperationException();
  }

  @Override
  public @Nullable String resolveTranslationDirectly(TranslationLanguage language) {
    return language.customTranslations.amount();
  }

  @Override
  public AmountKey getWrapped() {
    return this;
  }

  @Override
  public LangKeyedPredicateType getPredicateType() {
    return LangKeyedPredicateType.AMOUNT;
  }
}
