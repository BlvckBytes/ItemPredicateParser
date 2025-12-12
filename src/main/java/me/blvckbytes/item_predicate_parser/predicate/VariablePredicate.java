package me.blvckbytes.item_predicate_parser.predicate;

import me.blvckbytes.item_predicate_parser.token.Token;
import me.blvckbytes.item_predicate_parser.translation.TranslatedLangKeyed;
import me.blvckbytes.item_predicate_parser.translation.keyed.VariableKey;
import org.jetbrains.annotations.Nullable;

public record VariablePredicate(
  @Nullable Token token,
  TranslatedLangKeyed<VariableKey> translatedLangKeyed
) implements ItemPredicate {

  @Override
  public @Nullable ItemPredicate testForFailure(PredicateState state) {
    var variable = translatedLangKeyed.langKeyed.getWrapped();

    if (!variable.materials.isEmpty()) {
      if (variable.materials.stream().noneMatch(material -> material.equals(state.item.getType())))
        return this;
    }

    return null;
  }

  @Override
  public void stringify(StringifyState state) {
    if (token != null && (state.useTokens || translatedLangKeyed == null))
      state.appendString(token.stringify());
    else
      state.appendString(translatedLangKeyed.normalizedPrefixedTranslation);
  }
}
