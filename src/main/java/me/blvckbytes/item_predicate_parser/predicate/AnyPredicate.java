package me.blvckbytes.item_predicate_parser.predicate;

import me.blvckbytes.item_predicate_parser.predicate.stringify.StringifyHandler;
import me.blvckbytes.item_predicate_parser.token.Token;
import me.blvckbytes.item_predicate_parser.translation.TranslatedLangKeyed;
import me.blvckbytes.item_predicate_parser.translation.keyed.AnyKey;
import org.jetbrains.annotations.Nullable;

public record AnyPredicate(
  Token token,
  @Nullable TranslatedLangKeyed<AnyKey> translatedLangKeyed
) implements ItemPredicate {

  @Override
  public @Nullable ItemPredicate testForFailure(PredicateState state) {
    if (state.item.getType().isAir())
      return this;

    return null;
  }

  @Override
  public void stringify(StringifyHandler handler) {
    handler.stringify(this, output -> {
      if (handler.useTokens() || translatedLangKeyed == null)
        output.appendString(token.stringify());
      else
        output.appendString(translatedLangKeyed.normalizedPrefixedTranslation);
    });
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof AnyPredicate;
  }
}
