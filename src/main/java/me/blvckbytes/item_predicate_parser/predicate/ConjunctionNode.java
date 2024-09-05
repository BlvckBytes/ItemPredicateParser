package me.blvckbytes.item_predicate_parser.predicate;

import me.blvckbytes.item_predicate_parser.token.Token;
import me.blvckbytes.item_predicate_parser.translation.TranslatedTranslatable;
import org.jetbrains.annotations.Nullable;

public record ConjunctionNode(
  @Nullable Token token,
  TranslatedTranslatable translatedTranslatable,
  ItemPredicate lhs,
  ItemPredicate rhs,
  boolean wasImplicit
) implements ItemPredicate {

  public ConjunctionNode(
    @Nullable Token token,
    TranslatedTranslatable translatedTranslatable,
    ItemPredicate lhs,
    ItemPredicate rhs
  ) {
    this(token, translatedTranslatable, lhs, rhs, false);
  }

  @Override
  public boolean test(PredicateState state) {
    return lhs.test(state) && rhs.test(state);
  }

  @Override
  public String stringify(boolean useTokens) {
    if (wasImplicit)
      return lhs.stringify(useTokens) + " " + rhs.stringify(useTokens);

    if (useTokens && token != null)
      return lhs.stringify(true) + " " + token.stringify() + " " + rhs.stringify(true);

    return lhs.stringify(useTokens) + " " + translatedTranslatable.normalizedTranslation + " " + rhs.stringify(useTokens);
  }
}
