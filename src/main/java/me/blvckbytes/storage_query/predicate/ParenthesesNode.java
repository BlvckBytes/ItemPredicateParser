package me.blvckbytes.storage_query.predicate;

import org.bukkit.inventory.ItemStack;

import java.util.EnumSet;

public record ParenthesesNode (
  ItemPredicate inner
) implements ItemPredicate {

  @Override
  public boolean test(ItemStack item, EnumSet<PredicateFlags> flags) {
    return inner.test(item, flags);
  }

  @Override
  public String stringify(boolean useTokens) {
    return "(" + inner.stringify(useTokens) + ")";
  }
}
