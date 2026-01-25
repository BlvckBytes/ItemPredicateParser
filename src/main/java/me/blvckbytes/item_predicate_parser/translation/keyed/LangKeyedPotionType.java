package me.blvckbytes.item_predicate_parser.translation.keyed;

import org.bukkit.potion.PotionType;

import java.util.Objects;

public class LangKeyedPotionType implements LangKeyed<PotionType> {

  private final PotionType potionType;
  private final String languageFileKey;

  public LangKeyedPotionType(PotionType potionType) {
    this.potionType = potionType;

    var namespacedKey = potionType.getKey();
    this.languageFileKey = "item." + namespacedKey.getNamespace() + ".potion.effect." + namespacedKey.getKey();
  }

  @Override
  public String getLanguageFileKey() {
    return languageFileKey;
  }

  @Override
  public PotionType getWrapped() {
    return potionType;
  }

  @Override
  public LangKeyedPredicateType getPredicateType() {
    return LangKeyedPredicateType.POTION_TYPE;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof LangKeyedPotionType that)) return false;
    return Objects.equals(potionType, that.potionType);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(potionType);
  }

  @Override
  public String toString() {
    return potionType.toString();
  }
}
