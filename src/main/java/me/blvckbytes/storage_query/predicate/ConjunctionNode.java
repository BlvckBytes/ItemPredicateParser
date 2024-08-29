package me.blvckbytes.storage_query.predicate;

import me.blvckbytes.storage_query.token.Token;
import me.blvckbytes.storage_query.translation.TranslatedTranslatable;
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

    return lhs.stringify(useTokens) + " " + translatedTranslatable.normalizedName() + " " + rhs.stringify(useTokens);
  }
}
