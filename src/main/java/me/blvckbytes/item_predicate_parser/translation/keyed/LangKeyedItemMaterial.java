package me.blvckbytes.item_predicate_parser.translation.keyed;

import com.google.gson.JsonObject;
import me.blvckbytes.item_predicate_parser.translation.version.DetectedServerVersion;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class LangKeyedItemMaterial implements LangKeyed<Material> {

  private final Material material;
  private final String languageFileKey;

  public LangKeyedItemMaterial(Material material, DetectedServerVersion version, JsonObject languageJson) {
    if (!material.isItem())
      throw new IllegalStateException("Only supporting item materials");

    this.material = material;
    var namespacedKey = material.getKey();

    String fileKey;

    // NOTE: I don't see another way to know, without access to any kind of Registry, which
    //       materials are treated as items, and which as blocks, when it comes to the language
    //       file keys; isItem() and isBlock() do >not< indicate this relation.

    if ((fileKey = createPatchedFileKey(version, namespacedKey)) == null)
      fileKey = "item." + namespacedKey.getNamespace() + "." + namespacedKey.getKey();

    if (languageJson.get(fileKey) == null) {
      fileKey = "block." + namespacedKey.getNamespace() + "." + namespacedKey.getKey();

      if (languageJson.get(fileKey) == null)
        throw new IllegalStateException("Couldn't locate valid key for Material " + material + " (" + namespacedKey + ")");
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

  private static @Nullable String createPatchedFileKey(DetectedServerVersion version, NamespacedKey namespacedKey) {
    var key = namespacedKey.getKey();

    /*
      NETHERITE_UPGRADE_SMITHING_TEMPLATE:
        <= 1.20.X: item.minecraft.smithing_template
        >        : item.minecraft.netherite_upgrade_smithing_template
     */

    if (version.major() == 1 && version.minor() <= 20) {
      if (key.equals("netherite_upgrade_smithing_template"))
        return "item.minecraft.smithing_template";
    }

    /*
      Various armor trims:
      item.minecraft.>coast<_armor_trim_smithing_template => trim_pattern.minecraft.>coast<
     */

    var armorTrimMarker = "_armor_trim_smithing_template";

    int trimMarkerIndex;

    if ((trimMarkerIndex = key.indexOf(armorTrimMarker)) > 0) {
      var trimPattern = key.substring(0, trimMarkerIndex);
      return "trim_pattern.minecraft." + trimPattern;
    }

    return null;
  }
}
