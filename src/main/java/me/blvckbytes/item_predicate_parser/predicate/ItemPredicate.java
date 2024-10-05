package me.blvckbytes.item_predicate_parser.predicate;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public interface ItemPredicate extends Predicate<ItemStack> {

  default boolean test(PredicateState state) {
    return testForFailure(state) == null;
  }

  /**
   * @return null on match, the failing predicate otherwise
   */
  @Nullable ItemPredicate testForFailure(PredicateState state);

  default boolean test(ItemStack item) {
    return test(new PredicateState(item));
  }

  void stringify(StringifyState state);
}
