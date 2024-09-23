package me.blvckbytes.item_predicate_parser.predicate;

import me.blvckbytes.item_predicate_parser.token.Token;
import me.blvckbytes.item_predicate_parser.translation.TranslatedLangKeyed;
import org.jetbrains.annotations.Nullable;

public record ConjunctionNode(
  @Nullable Token token,
  TranslatedLangKeyed<?> translatedLangKeyed,
  ItemPredicate lhs,
  ItemPredicate rhs
) implements ItemPredicate {

  @Override
  public boolean test(PredicateState state) {
    return lhs.test(state) && rhs.test(state);
  }

  @Override
  public String stringify(boolean useTokens) {
    if (token == null)
      return lhs.stringify(useTokens) + " " + rhs.stringify(useTokens);

    if (useTokens)
      return lhs.stringify(true) + " " + token.stringify() + " " + rhs.stringify(true);

    return lhs.stringify(false) + " " + translatedLangKeyed.normalizedPrefixedTranslation + " " + rhs.stringify(false);
  }
}
