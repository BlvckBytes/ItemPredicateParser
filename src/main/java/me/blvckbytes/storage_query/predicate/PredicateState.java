package me.blvckbytes.storage_query.predicate;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class PredicateState {

  public final ItemStack item;
  // Passing down the meta spares the need to access it over and
  // over again, which internally causes useless allocations
  public final @Nullable ItemMeta meta;
  public final EnumSet<PredicateFlags> flags;

  public PredicateState(ItemStack item) {
    this.item = item;
    this.meta = item.getItemMeta();
    this.flags = EnumSet.noneOf(PredicateFlags.class);
  }
}
