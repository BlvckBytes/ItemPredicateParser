package me.blvckbytes.item_predicate_parser.predicate;

import me.blvckbytes.item_predicate_parser.predicate.stringify.StringifyHandler;
import org.jetbrains.annotations.Nullable;

public record ParenthesesNode (
  ItemPredicate inner
) implements UnaryNode {

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
  public boolean equals(Object other) {
    if (!(other instanceof ParenthesesNode otherPredicate))
      return false;

    return this.inner.equals(otherPredicate.inner);
  }

  @Override
  public ItemPredicate getOperand() {
    return inner;
  }

  @Override
  public UnaryNode cloneWithNewOperand(ItemPredicate newOperand) {
    return new ParenthesesNode(newOperand);
  }
}
