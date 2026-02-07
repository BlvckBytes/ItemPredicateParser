package me.blvckbytes.item_predicate_parser.predicate;

import me.blvckbytes.item_predicate_parser.predicate.stringify.StringifyHandler;
import me.blvckbytes.item_predicate_parser.token.IntegerToken;
import me.blvckbytes.item_predicate_parser.token.Token;
import me.blvckbytes.item_predicate_parser.translation.keyed.AmountKey;
import me.blvckbytes.item_predicate_parser.translation.TranslatedLangKeyed;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public record AmountPredicate(
  Token token,
  TranslatedLangKeyed<AmountKey> translatedLangKeyed,
  IntegerToken amountArgument
) implements ItemPredicate {

  @Override
  public @Nullable ItemPredicate testForFailure(PredicateState state) {
    if (amountArgument.matches(state.item.getAmount()))
      return null;

    return this;
  }

  @Override
  public void stringify(StringifyHandler handler) {
    handler.stringify(this, output -> {
      if (handler.useTokens())
        output.appendString(token.stringify());
      else
        output.appendString(translatedLangKeyed.normalizedPrefixedTranslation);

      output.appendSpace();
      output.appendString(amountArgument.stringify());
    });
  }

  @Override
  public boolean containsOrEqualsPredicate(ItemPredicate node, EnumSet<ComparisonFlag> comparisonFlags) {
    return equals(node);
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof AmountPredicate otherPredicate))
      return false;

    return amountArgument.equals(otherPredicate.amountArgument);
  }
}
