package me.blvckbytes.item_predicate_parser.predicate;

import me.blvckbytes.item_predicate_parser.token.Token;
import me.blvckbytes.item_predicate_parser.translation.TranslatedLangKeyed;

public record ExactNode(
  Token token,
  TranslatedLangKeyed<?> translatedLangKeyed,
  ItemPredicate operand
) implements ItemPredicate {

  @Override
  public boolean test(PredicateState state) {
    var exactState = state.copyAndEnterExact();

    // The predicates themselves weren't satisfied
    if (!operand.test(exactState))
        return false;

    // There have been remaining, unmatched properties - exact-mode failed
    return !exactState.hasRemains();
  }

  @Override
  public String stringify(boolean useTokens) {
    if (useTokens)
      return token.stringify() + " " + operand.stringify(true);

    return translatedLangKeyed.normalizedPrefixedTranslation + " " + operand.stringify(false);
  }
}
