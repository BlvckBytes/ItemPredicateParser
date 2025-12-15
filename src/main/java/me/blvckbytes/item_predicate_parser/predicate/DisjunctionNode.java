package me.blvckbytes.item_predicate_parser.predicate;

import me.blvckbytes.item_predicate_parser.token.Token;
import me.blvckbytes.item_predicate_parser.translation.TranslatedLangKeyed;
import org.jetbrains.annotations.Nullable;

public record DisjunctionNode(
  Token token,
  TranslatedLangKeyed<?> translatedLangKeyed,
  ItemPredicate lhs,
  ItemPredicate rhs
) implements ItemPredicate {

  @Override
  public @Nullable ItemPredicate testForFailure(PredicateState state) {
    if (lhs.testForFailure(state) == null)
      return null;

    if (rhs.testForFailure(state) == null)
      return null;

    return this;
  }

  @Override
  public void stringify(StringifyState state) {
    state.appendPredicate(lhs);
    state.appendSpace();

    if (state.useTokens)
      state.appendString(token.stringify());
    else
      state.appendString(translatedLangKeyed.normalizedPrefixedTranslation);

    state.appendSpace();
    state.appendPredicate(rhs);
  }

  @Override
  public boolean isTransitiveParentTo(ItemPredicate node) {
    return lhs == node || lhs.isTransitiveParentTo(node) || rhs == node || rhs.isTransitiveParentTo(node);
  }
}
