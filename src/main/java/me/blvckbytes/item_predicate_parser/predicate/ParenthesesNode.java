package me.blvckbytes.item_predicate_parser.predicate;

import me.blvckbytes.item_predicate_parser.predicate.stringify.StringifyHandler;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public record ParenthesesNode (
  ItemPredicate inner
) implements ItemPredicate {

  @Override
  public @Nullable ItemPredicate testForFailure(PredicateState state) {
    return inner.testForFailure(state);
  }

  @Override
  public void stringify(StringifyHandler handler) {
    handler.stringify(this, output -> output.appendString("("));
    inner.stringify(handler);
    handler.stringify(this, output -> output.appendString(")"));
  }

  @Override
  public boolean containsOrEqualsPredicate(ItemPredicate node, EnumSet<ComparisonFlag> comparisonFlags) {
    return equals(node) || inner.containsOrEqualsPredicate(node, comparisonFlags);
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof ParenthesesNode otherPredicate))
      return false;

    return this.inner.equals(otherPredicate.inner);
  }
}
