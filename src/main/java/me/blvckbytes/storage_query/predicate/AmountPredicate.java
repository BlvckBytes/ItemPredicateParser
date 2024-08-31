package me.blvckbytes.storage_query.predicate;

import me.blvckbytes.storage_query.token.IntegerToken;
import me.blvckbytes.storage_query.token.Token;
import me.blvckbytes.storage_query.translation.TranslatedTranslatable;

public record AmountPredicate(
  Token token,
  TranslatedTranslatable translatedTranslatable,
  IntegerToken amountArgument
) implements ItemPredicate {

  @Override
  public boolean test(PredicateState state) {
    return amountArgument.matches(state.item.getAmount());
  }

  @Override
  public String stringify(boolean useTokens) {
    if (useTokens)
      return token.stringify() + " " + amountArgument.stringify();

    return translatedTranslatable.normalizedName + " " + amountArgument.stringify();
  }
}
