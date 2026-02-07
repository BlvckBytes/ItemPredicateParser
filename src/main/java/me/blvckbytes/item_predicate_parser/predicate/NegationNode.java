package me.blvckbytes.item_predicate_parser.predicate;

import me.blvckbytes.item_predicate_parser.predicate.stringify.StringifyHandler;
import me.blvckbytes.item_predicate_parser.token.Token;
import me.blvckbytes.item_predicate_parser.translation.TranslatedLangKeyed;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

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
    if (!(other instanceof NegationNode otherPredicate))
      return false;

    return this.operand.equals(otherPredicate.operand);
  }
}
