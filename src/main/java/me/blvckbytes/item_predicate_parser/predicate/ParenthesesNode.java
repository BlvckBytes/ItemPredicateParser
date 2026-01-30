package me.blvckbytes.item_predicate_parser.predicate;

import me.blvckbytes.item_predicate_parser.predicate.stringify.StringifyHandler;
import org.jetbrains.annotations.Nullable;

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
  public boolean isTransitiveParentTo(ItemPredicate node) {
    return inner == node || inner.isTransitiveParentTo(node);
  }
}
