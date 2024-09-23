package me.blvckbytes.item_predicate_parser.translation.keyed;

import org.bukkit.enchantments.Enchantment;

import java.util.Objects;

public class LangKeyedEnchantment implements LangKeyed<Enchantment> {

  private final Enchantment enchantment;
  private final String languageFileKey;

  public LangKeyedEnchantment(Enchantment enchantment) {
    this.enchantment = enchantment;

    var namespacedKey = enchantment.getKey();
    this.languageFileKey = "enchantment." + namespacedKey.getNamespace() + "." + namespacedKey.getKey();
  }

  @Override
  public String getLanguageFileKey() {
    return languageFileKey;
  }

  @Override
  public Enchantment getWrapped() {
    return enchantment;
  }

  @Override
  public LangKeyedPredicateType getPredicateType() {
    return LangKeyedPredicateType.ENCHANTMENT;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof LangKeyedEnchantment that)) return false;
    return Objects.equals(enchantment, that.enchantment);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(enchantment);
  }

  @Override
  public String toString() {
    return enchantment.toString();
  }
}
