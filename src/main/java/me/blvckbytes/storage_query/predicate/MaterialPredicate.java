package me.blvckbytes.storage_query.predicate;

import me.blvckbytes.storage_query.token.UnquotedStringToken;
import me.blvckbytes.storage_query.translation.TranslatedTranslatable;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record MaterialPredicate(
  @Nullable TranslatedTranslatable translatedTranslatable,
  UnquotedStringToken searchToken,
  List<Material> materials
) implements ItemPredicate {

  @Override
  public boolean test(ItemStack item) {
    for (var material : materials) {
      if (material.equals(item.getType()))
        return true;
    }
    return false;
  }

  @Override
  public String stringify() {
    if (translatedTranslatable != null)
      return translatedTranslatable.normalizedName();

    return searchToken.value();
  }
}
