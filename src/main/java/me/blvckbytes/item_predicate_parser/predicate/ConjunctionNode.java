package me.blvckbytes.item_predicate_parser.predicate;

import me.blvckbytes.item_predicate_parser.predicate.stringify.StringifyHandler;
import me.blvckbytes.item_predicate_parser.token.Token;
import me.blvckbytes.item_predicate_parser.translation.TranslatedLangKeyed;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

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
  public void stringify(StringifyHandler handler) {
    lhs.stringify(handler);

    handler.stringify(this, output -> {
      if (token != null) {
        output.appendSpace();

        if (handler.useTokens())
          output.appendString(token.stringify());
        else
          output.appendString(translatedLangKeyed.normalizedPrefixedTranslation);
      }

      output.appendSpace();
    });

    rhs.stringify(handler);
  }

  @Override
  public boolean containsOrEqualsPredicate(ItemPredicate node, EnumSet<ComparisonFlag> comparisonFlags) {
    return equals(node) || lhs.containsOrEqualsPredicate(node, comparisonFlags) || rhs.containsOrEqualsPredicate(node, comparisonFlags);
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof ConjunctionNode otherPredicate))
      return false;

    if (!this.rhs.equals(otherPredicate.rhs))
      return false;

    return this.lhs.equals(otherPredicate.lhs);
  }
}
