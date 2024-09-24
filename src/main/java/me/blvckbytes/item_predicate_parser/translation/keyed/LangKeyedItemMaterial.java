package me.blvckbytes.item_predicate_parser.translation.keyed;

import com.google.gson.JsonObject;
import org.bukkit.Material;

import java.util.Objects;

public class LangKeyedItemMaterial implements LangKeyed<Material> {

  private final Material material;
  private final String languageFileKey;

  public LangKeyedItemMaterial(Material material, JsonObject languageJson) {
    if (!material.isItem())
      throw new IllegalStateException("Only supporting item materials");

    this.material = material;

    // NOTE: I don't see another way to know, without access to any kind of Registry, which
    //       materials are treated as items, and which as blocks, when it comes to the language
    //       file keys; isItem() and isBlock() do >not< indicate this relation.

    var namespacedKey = material.getKey();
    var fileKey = "item." + namespacedKey.getNamespace() + "." + namespacedKey.getKey();

    if (languageJson.get(fileKey) == null) {
      fileKey = "block." + namespacedKey.getNamespace() + "." + namespacedKey.getKey();

      if (languageJson.get(fileKey) == null)
        throw new IllegalStateException("Couldn't locate valid key for Material " + material);
    }

    this.languageFileKey = fileKey;
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
