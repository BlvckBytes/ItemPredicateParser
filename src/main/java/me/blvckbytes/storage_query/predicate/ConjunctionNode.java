package me.blvckbytes.storage_query.predicate;

import me.blvckbytes.storage_query.translation.TranslatedTranslatable;
import org.bukkit.inventory.ItemStack;

public record ConjunctionNode(
  TranslatedTranslatable translatedTranslatable,
  ItemPredicate lhs,
  ItemPredicate rhs,
  boolean wasImplicit
) implements ItemPredicate {

  @Override
  public boolean test(ItemStack item) {
    return lhs.test(item) && rhs.test(item);
  }

  @Override
  public String stringify() {
    if (wasImplicit)
      return lhs.stringify() + " " + rhs.stringify();

    return lhs.stringify() + " " + translatedTranslatable.normalizedName() + " " + rhs.stringify();
  }
}
