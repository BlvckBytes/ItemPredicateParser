package me.blvckbytes.item_predicate_parser.predicate;

import me.blvckbytes.item_predicate_parser.token.Token;
import me.blvckbytes.item_predicate_parser.translation.TranslatedTranslatable;

public record DisjunctionNode(
  Token token,
  TranslatedTranslatable translatedTranslatable,
  ItemPredicate lhs,
  ItemPredicate rhs
) implements ItemPredicate {

  @Override
  public boolean test(PredicateState state) {
    return lhs.test(state) || rhs.test(state);
  }

  @Override
  public String stringify(boolean useTokens) {
    if (useTokens)
      return lhs.stringify(true) + " " + token.stringify() + " " + rhs.stringify(true);

    return lhs.stringify(false) + " " + translatedTranslatable.normalizedTranslation + " " + rhs.stringify(false);
  }
}
