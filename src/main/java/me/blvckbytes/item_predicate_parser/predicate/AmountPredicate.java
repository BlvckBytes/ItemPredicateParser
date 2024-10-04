package me.blvckbytes.item_predicate_parser.predicate;

import me.blvckbytes.item_predicate_parser.token.IntegerToken;
import me.blvckbytes.item_predicate_parser.token.Token;
import me.blvckbytes.item_predicate_parser.translation.keyed.AmountKey;
import me.blvckbytes.item_predicate_parser.translation.TranslatedLangKeyed;
import org.jetbrains.annotations.Nullable;

public record AmountPredicate(
  Token token,
  TranslatedLangKeyed<AmountKey> translatedLangKeyed,
  IntegerToken amountArgument
) implements ItemPredicate {

  @Override
  public @Nullable ItemPredicate testForFailure(PredicateState state) {
    if (amountArgument.matches(state.item.getAmount()))
      return null;

    return this;
  }

  @Override
  public String stringify(boolean useTokens) {
    if (useTokens)
      return token.stringify() + " " + amountArgument.stringify();

    return translatedLangKeyed.normalizedPrefixedTranslation + " " + amountArgument.stringify();
  }
}
