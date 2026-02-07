package me.blvckbytes.item_predicate_parser.predicate;

import me.blvckbytes.item_predicate_parser.predicate.stringify.StringifyHandler;
import me.blvckbytes.item_predicate_parser.token.Token;
import me.blvckbytes.item_predicate_parser.translation.TranslatedLangKeyed;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public record ExactNode(
  Token token,
  TranslatedLangKeyed<?> translatedLangKeyed,
  ItemPredicate operand
) implements ItemPredicate {

  @Override
  public @Nullable ItemPredicate testForFailure(PredicateState state) {
    var exactState = state.copyAndEnterExact();
    ItemPredicate failure;

    // The predicates themselves weren't satisfied
    if ((failure = operand.testForFailure(exactState)) != null)
        return failure;

    // There have been remaining, unmatched properties - exact-mode failed
    if (exactState.hasRemains())
      return this;

    return null;
  }

  @Override
  public void stringify(StringifyHandler handler) {
    handler.stringify(this, output -> {
      if (handler.useTokens())
        output.appendString(token.stringify());
      else
        output.appendString(translatedLangKeyed.normalizedPrefixedTranslation);

      if (!(operand instanceof ParenthesesNode))
        output.appendSpace();
    });

    operand.stringify(handler);
  }

  @Override
  public boolean containsOrEqualsPredicate(ItemPredicate node, EnumSet<ComparisonFlag> comparisonFlags) {
    return equals(node) || operand.containsOrEqualsPredicate(node, comparisonFlags);
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof ExactNode otherNode))
      return false;

    return this.operand.equals(otherNode.operand);
  }
}
