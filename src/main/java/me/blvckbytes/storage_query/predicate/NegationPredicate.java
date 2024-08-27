package me.blvckbytes.storage_query.predicate;

import me.blvckbytes.storage_query.translation.TranslatedTranslatable;
import org.bukkit.inventory.ItemStack;

public record NegationPredicate(
  TranslatedTranslatable translatedTranslatable,
  ItemPredicate operand
) implements ItemPredicate {

  @Override
  public boolean test(ItemStack item) {
    return !operand.test(item);
  }

  @Override
  public String stringify() {
    return translatedTranslatable.normalizedName() + " " + operand.stringify();
  }
}
