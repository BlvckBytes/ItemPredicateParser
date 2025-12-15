package me.blvckbytes.item_predicate_parser.predicate;

import me.blvckbytes.item_predicate_parser.token.Token;
import me.blvckbytes.item_predicate_parser.translation.TranslatedLangKeyed;
import org.jetbrains.annotations.Nullable;

public record NegationNode(
  Token token,
  TranslatedLangKeyed<?> translatedLangKeyed,
  ItemPredicate operand
) implements ItemPredicate {

  @Override
  public @Nullable ItemPredicate testForFailure(PredicateState state) {
    if (operand.testForFailure(state) == null)
      return this;

    return null;
  }

  @Override
  public void stringify(StringifyState state) {
    if (state.useTokens)
      state.appendString(token.stringify());
    else
      state.appendString(translatedLangKeyed.normalizedPrefixedTranslation);

    if (!(operand instanceof ParenthesesNode))
      state.appendSpace();

    state.appendPredicate(operand);
  }

  @Override
  public boolean isTransitiveParentTo(ItemPredicate node) {
    return operand == node || operand.isTransitiveParentTo(node);
  }
}
