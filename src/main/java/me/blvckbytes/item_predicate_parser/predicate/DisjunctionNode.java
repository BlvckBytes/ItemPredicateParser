package me.blvckbytes.item_predicate_parser.predicate;

import me.blvckbytes.item_predicate_parser.predicate.stringify.StringifyHandler;
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
  public void stringify(StringifyHandler handler) {
    lhs.stringify(handler);

    handler.stringify(this, output -> {
      output.appendSpace();

      if (handler.useTokens())
        output.appendString(token.stringify());
      else
        output.appendString(translatedLangKeyed.normalizedPrefixedTranslation);

      output.appendSpace();
    });

    rhs.stringify(handler);
  }

  @Override
  public boolean isTransitiveParentTo(ItemPredicate node) {
    return lhs == node || lhs.isTransitiveParentTo(node) || rhs == node || rhs.isTransitiveParentTo(node);
  }
}
