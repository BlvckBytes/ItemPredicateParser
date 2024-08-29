package me.blvckbytes.storage_query.predicate;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public interface ItemPredicate {

  // Passing down the meta spares the need to access it over and
  // over again, which internally causes useless allocations
  boolean test(
    ItemStack item,
    @Nullable ItemMeta meta,
    EnumSet<PredicateFlags> flags
  );

  /**
   * Stringifies the predicate to represent the fully-expanded version of it's previously
   * parsed arguments in the same style, i.e. space-separated values
   * @param useTokens When using tokens, stringification possibly yields abbreviations as
   *                  entered; otherwise, translatables will be made use of.
   */
  String stringify(boolean useTokens);
}
