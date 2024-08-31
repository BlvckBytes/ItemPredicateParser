package me.blvckbytes.storage_query.predicate;

import me.blvckbytes.storage_query.token.Token;
import me.blvckbytes.storage_query.translation.TranslatedTranslatable;

public record ExactNode(
  Token token,
  TranslatedTranslatable translatedTranslatable,
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

    return translatedTranslatable.normalizedName + " " + operand.stringify(false);
  }
}
