package me.blvckbytes.item_predicate_parser.predicate;

public record ParenthesesNode (
  ItemPredicate inner
) implements ItemPredicate {

  @Override
  public boolean test(PredicateState state) {
    return inner.test(state);
  }

  @Override
  public String stringify(boolean useTokens) {
    return "(" + inner.stringify(useTokens) + ")";
  }
}
