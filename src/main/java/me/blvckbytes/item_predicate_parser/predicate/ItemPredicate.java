package me.blvckbytes.item_predicate_parser.predicate;

import me.blvckbytes.item_predicate_parser.predicate.stringify.StringifyHandler;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
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

  default boolean containsOrEqualsPredicate(ItemPredicate node, EnumSet<ComparisonFlag> comparisonFlags) {
    if (equals(node))
      return true;

    if (this instanceof UnaryNode unaryNode)
      return unaryNode.getOperand().containsOrEqualsPredicate(node, comparisonFlags);

    if (this instanceof BinaryNode binaryNode) {
      if (binaryNode.getLHS().containsOrEqualsPredicate(node, comparisonFlags))
        return true;

      return binaryNode.getRHS().containsOrEqualsPredicate(node, comparisonFlags);
    }

    return false;
  }

  /**
   * @return Clone with the removal carried out; null if there was nothing left.
   */
  default @Nullable ItemPredicate removeNodes(Predicate<ItemPredicate> condition) {
    if (condition.test(this))
      return null;

    if (this instanceof UnaryNode unaryNode) {
      var newOperand = unaryNode.getOperand().removeNodes(condition);

      if (newOperand == null)
        return null;

      return unaryNode.cloneWithNewOperand(newOperand);
    }

    if (this instanceof BinaryNode binaryNode) {
      var newLhs = binaryNode.getLHS().removeNodes(condition);
      var newRhs = binaryNode.getRHS().removeNodes(condition);

      if (newLhs != null && newRhs != null)
        return binaryNode.cloneWithNewOperands(newLhs, newRhs);

      if (newLhs == null && newRhs == null)
        return null;

      if (newLhs == null)
        return newRhs;

      return newLhs;
    }

    return this;
  }
}
