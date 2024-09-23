package me.blvckbytes.item_predicate_parser.translation.keyed;

import org.bukkit.Material;

import java.util.Objects;

public class LangKeyedItemMaterial implements LangKeyed<Material> {

  private final Material material;
  private final String languageFileKey;

  public LangKeyedItemMaterial(Material material) {
    if (!material.isItem())
      throw new IllegalStateException("Only supporting item materials");

    this.material = material;

    // TODO: Find out how to decide on whether a material is block or item (isItem does not help)
    //       Then, replace this, as to be truly independent of the Translatable-Interface
    this.languageFileKey = material.getTranslationKey();
  }

  @Override
  public String getLanguageFileKey() {
    return languageFileKey;
  }

  @Override
  public Material getWrapped() {
    return material;
  }

  @Override
  public LangKeyedPredicateType getPredicateType() {
    return LangKeyedPredicateType.ITEM_MATERIAL;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof LangKeyedItemMaterial that)) return false;
    return material == that.material;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(material);
  }

  @Override
  public String toString() {
    return material.toString();
  }
}
