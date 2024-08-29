package me.blvckbytes.storage_query.predicate;

import me.blvckbytes.storage_query.token.Token;
import me.blvckbytes.storage_query.translation.TranslatedTranslatable;

public record NegationNode(
  Token token,
  TranslatedTranslatable translatedTranslatable,
  ItemPredicate operand
) implements ItemPredicate {

  @Override
  public boolean test(PredicateState state) {
    return !operand.test(state);
  }

  @Override
  public String stringify(boolean useTokens) {
    if (useTokens)
      return token.stringify() + " " + operand.stringify(true);

    return translatedTranslatable.normalizedName() + " " + operand.stringify(false);
  }
}
