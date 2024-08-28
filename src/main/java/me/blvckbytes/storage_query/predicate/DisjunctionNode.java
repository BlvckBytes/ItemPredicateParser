package me.blvckbytes.storage_query.predicate;

import me.blvckbytes.storage_query.translation.TranslatedTranslatable;
import org.bukkit.inventory.ItemStack;

public record DisjunctionNode(
  TranslatedTranslatable translatedTranslatable,
  ItemPredicate lhs,
  ItemPredicate rhs
) implements ItemPredicate {

  @Override
  public boolean test(ItemStack item) {
    return lhs.test(item) || rhs.test(item);
  }

  @Override
  public String stringify() {
    return lhs.stringify() + " " + translatedTranslatable.normalizedName() + " " + rhs.stringify();
  }
}
