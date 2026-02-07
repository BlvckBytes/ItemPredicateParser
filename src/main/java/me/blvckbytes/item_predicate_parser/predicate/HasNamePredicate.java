package me.blvckbytes.item_predicate_parser.predicate;

import me.blvckbytes.item_predicate_parser.predicate.stringify.StringifyHandler;
import me.blvckbytes.item_predicate_parser.token.Token;
import me.blvckbytes.item_predicate_parser.translation.TranslatedLangKeyed;
import me.blvckbytes.item_predicate_parser.translation.keyed.HasNameKey;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public record HasNamePredicate(
  Token token,
  @Nullable TranslatedLangKeyed<HasNameKey> translatedLangKeyed
) implements ItemPredicate {

  @Override
  public @Nullable ItemPredicate testForFailure(PredicateState state) {
    var meta = state.getMeta();

    if (meta == null)
      return this;

    if (!meta.hasDisplayName() && !meta.hasCustomName())
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
  public boolean containsOrEqualsPredicate(ItemPredicate node, EnumSet<ComparisonFlag> comparisonFlags) {
    return equals(node);
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof HasNamePredicate;
  }
}
