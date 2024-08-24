package me.blvckbytes.storage_query.predicate;

import me.blvckbytes.storage_query.translation.TranslatedTranslatable;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public record MaterialPredicate(
  TranslatedTranslatable translatedTranslatable,
  Material material
) implements ItemPredicate {

  @Override
  public boolean test(ItemStack item) {
    return item.getType().equals(this.material);
  }

  @Override
  public String stringify() {
    return translatedTranslatable.normalizedName();
  }
}
