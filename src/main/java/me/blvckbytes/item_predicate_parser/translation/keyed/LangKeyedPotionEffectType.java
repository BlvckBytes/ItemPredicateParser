package me.blvckbytes.item_predicate_parser.translation.keyed;

import org.bukkit.potion.PotionEffectType;

import java.util.Objects;

public class LangKeyedPotionEffectType implements LangKeyed<PotionEffectType> {

  private final PotionEffectType effectType;
  private final String languageFileKey;

  public LangKeyedPotionEffectType(PotionEffectType effectType) {
    this.effectType = effectType;

    var namespacedKey = effectType.getKey();
    this.languageFileKey = "effect." + namespacedKey.getNamespace() + "." + namespacedKey.getKey();
  }

  @Override
  public String getLanguageFileKey() {
    return languageFileKey;
  }

  @Override
  public PotionEffectType getWrapped() {
    return effectType;
  }

  @Override
  public LangKeyedPredicateType getPredicateType() {
    return LangKeyedPredicateType.POTION_EFFECT_TYPE;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof LangKeyedPotionEffectType that)) return false;
    return Objects.equals(effectType, that.effectType);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(effectType);
  }

  @Override
  public String toString() {
    return effectType.toString();
  }
}
