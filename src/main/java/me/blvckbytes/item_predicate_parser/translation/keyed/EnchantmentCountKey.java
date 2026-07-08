package me.blvckbytes.item_predicate_parser.translation.keyed;

import me.blvckbytes.item_predicate_parser.translation.TranslationLanguage;
import org.jetbrains.annotations.Nullable;

public class EnchantmentCountKey implements LangKeyed<EnchantmentCountKey> {

  public static final EnchantmentCountKey INSTANCE = new EnchantmentCountKey();

  private EnchantmentCountKey() {}

  @Override
  public String getLanguageFileKey() {
    throw new UnsupportedOperationException();
  }

  @Override
  public @Nullable String resolveTranslationDirectly(TranslationLanguage language) {
    return language.customTranslations.enchantmentCount();
  }

  @Override
  public EnchantmentCountKey getWrapped() {
    return this;
  }

  @Override
  public LangKeyedPredicateType getPredicateType() {
    return LangKeyedPredicateType.ENCHANTMENT_COUNT;
  }
}
