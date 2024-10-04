package me.blvckbytes.item_predicate_parser.predicate;

import me.blvckbytes.item_predicate_parser.token.Token;
import me.blvckbytes.item_predicate_parser.translation.TranslatedLangKeyed;
import org.jetbrains.annotations.Nullable;

public record DisjunctionNode(
  Token token,
  TranslatedLangKeyed<?> translatedLangKeyed,
  ItemPredicate lhs,
  ItemPredicate rhs
) implements ItemPredicate {

  @Override
  public @Nullable ItemPredicate testForFailure(PredicateState state) {
    if (lhs.testForFailure(state) == null)
      return null;

    if (rhs.testForFailure(state) == null)
      return null;

    return this;
  }

  @Override
  public String stringify(boolean useTokens) {
    if (useTokens)
      return lhs.stringify(true) + " " + token.stringify() + " " + rhs.stringify(true);

    return lhs.stringify(false) + " " + translatedLangKeyed.normalizedPrefixedTranslation + " " + rhs.stringify(false);
  }
}
