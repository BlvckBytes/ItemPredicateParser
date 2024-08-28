package me.blvckbytes.storage_query.predicate;

import org.bukkit.inventory.ItemStack;

public record ParenthesesNode (
  ItemPredicate inner
) implements ItemPredicate {

  @Override
  public boolean test(ItemStack item) {
    return inner.test(item);
  }

  @Override
  public String stringify() {
    return "(" + inner.stringify() + ")";
  }
}
