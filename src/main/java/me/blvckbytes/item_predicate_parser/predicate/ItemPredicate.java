package me.blvckbytes.item_predicate_parser.predicate;

import me.blvckbytes.item_predicate_parser.predicate.stringify.StringifyHandler;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public interface ItemPredicate extends Predicate<ItemStack> {

  ItemStack AIR_ITEM = new ItemStack(Material.AIR);

  default boolean test(PredicateState state) {
    return testForFailure(state) == null;
  }

  /**
   * @return null on match, the failing predicate otherwise
   */
  @Nullable ItemPredicate testForFailure(PredicateState state);

  default boolean test(ItemStack item) {
    if (item == null)
      item = AIR_ITEM;

    return test(new PredicateState(item));
  }

  void stringify(StringifyHandler handler);

  boolean isTransitiveParentTo(ItemPredicate node);

}
