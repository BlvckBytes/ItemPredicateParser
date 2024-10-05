package me.blvckbytes.item_predicate_parser.predicate;

import org.jetbrains.annotations.Nullable;

public record ParenthesesNode (
  ItemPredicate inner
) implements ItemPredicate {

  @Override
  public @Nullable ItemPredicate testForFailure(PredicateState state) {
    return inner.testForFailure(state);
  }

  @Override
  public void stringify(StringifyState state) {
    state.appendString("(");
    state.appendPredicate(inner);
    state.appendString(")");
  }
}
