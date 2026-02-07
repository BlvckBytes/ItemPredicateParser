package me.blvckbytes.item_predicate_parser.predicate;

import me.blvckbytes.item_predicate_parser.predicate.stringify.StringifyHandler;
import me.blvckbytes.item_predicate_parser.token.Token;
import me.blvckbytes.item_predicate_parser.translation.TranslatedLangKeyed;
import org.jetbrains.annotations.Nullable;

public record ConjunctionNode(
  @Nullable Token token,
  TranslatedLangKeyed<?> translatedLangKeyed,
  ItemPredicate lhs,
  ItemPredicate rhs
) implements BinaryNode {

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
  public boolean equals(Object other) {
    if (!(other instanceof ConjunctionNode otherPredicate))
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
    return new ConjunctionNode(token, translatedLangKeyed, newLhs, newRhs);
  }
}
