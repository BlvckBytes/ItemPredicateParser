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
  public void stringify(StringifyState state) {
    if (state.useTokens)
      state.appendString(token.stringify());
    else
      state.appendString(translatedLangKeyed.normalizedPrefixedTranslation);

    state.appendSpace();
    state.appendString(amountArgument.stringify());
  }
}
