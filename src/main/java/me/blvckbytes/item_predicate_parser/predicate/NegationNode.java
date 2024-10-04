package me.blvckbytes.item_predicate_parser.predicate;

import me.blvckbytes.item_predicate_parser.token.Token;
import me.blvckbytes.item_predicate_parser.translation.TranslatedLangKeyed;
import org.jetbrains.annotations.Nullable;

public record NegationNode(
  Token token,
  TranslatedLangKeyed<?> translatedLangKeyed,
  ItemPredicate operand
) implements ItemPredicate {

  @Override
  public @Nullable ItemPredicate testForFailure(PredicateState state) {
    if (operand.testForFailure(state) == null)
      return this;

    return null;
  }

  @Override
  public String stringify(boolean useTokens) {
    if (useTokens)
      return token.stringify() + " " + operand.stringify(true);

    return translatedLangKeyed.normalizedPrefixedTranslation + " " + operand.stringify(false);
  }
}
