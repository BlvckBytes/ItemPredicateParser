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
  public @Nullable ItemPredicate testForFailure(PredicateState state) {
    ItemPredicate failure;

    if ((failure = lhs.testForFailure(state)) != null)
      return failure;

    if ((failure = rhs.testForFailure(state)) != null)
      return failure;

    return null;
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
