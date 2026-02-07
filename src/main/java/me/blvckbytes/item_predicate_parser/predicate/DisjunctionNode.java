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
) implements BinaryNode {

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
  public boolean equals(Object other) {
    if (!(other instanceof DisjunctionNode otherPredicate))
      return false;

    if (!this.rhs.equals(otherPredicate.rhs))
      return false;

    return this.lhs.equals(otherPredicate.lhs);
  }

  @Override
  public ItemPredicate getLHS() {
    return lhs;
  }

  @Override
  public ItemPredicate getRHS() {
    return rhs;
  }

  @Override
  public BinaryNode cloneWithNewOperands(ItemPredicate newLhs, ItemPredicate newRhs) {
    return new DisjunctionNode(token, translatedLangKeyed, newLhs, newRhs);
  }
}
