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
    if (state.flags.contains(PredicateFlags.EXACT_MODE)) {
      // TODO: Implement correct behavior
    }

    return operand.test(state);
  }

  @Override
  public String stringify(boolean useTokens) {
    if (useTokens)
      return token.stringify() + " " + operand.stringify(true);

    return translatedTranslatable.normalizedName() + " " + operand.stringify(false);
  }
}
