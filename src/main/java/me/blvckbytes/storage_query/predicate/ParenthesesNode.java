package me.blvckbytes.storage_query.predicate;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public record ParenthesesNode (
  ItemPredicate inner
) implements ItemPredicate {

  @Override
  public boolean test(ItemStack item, @Nullable ItemMeta meta, EnumSet<PredicateFlags> flags) {
    return inner.test(item, meta, flags);
  }

  @Override
  public String stringify(boolean useTokens) {
    return "(" + inner.stringify(useTokens) + ")";
  }
}
