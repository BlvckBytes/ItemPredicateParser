package me.blvckbytes.item_predicate_parser.predicate;

import me.blvckbytes.item_predicate_parser.token.Token;
import me.blvckbytes.item_predicate_parser.translation.TranslatedLangKeyed;

public record NegationNode(
  Token token,
  TranslatedLangKeyed translatedLangKeyed,
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

    return translatedLangKeyed.normalizedPrefixedTranslation + " " + operand.stringify(false);
  }
}
