package me.blvckbytes.storage_query.predicate;

import org.bukkit.inventory.ItemStack;

public record TextSearchPredicate(String text) implements ItemPredicate {

  @Override
  public boolean test(ItemStack item) {
    var meta = item.getItemMeta();

    if (meta == null)
      return false;

    // TODO: Implement

    return false;
  }

  @Override
  public String stringify() {
    return "\"" + text + "\"";
  }
}
