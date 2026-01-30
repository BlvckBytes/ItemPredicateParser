package me.blvckbytes.item_predicate_parser.predicate;

import me.blvckbytes.item_predicate_parser.predicate.stringify.StringifyHandler;
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
    var materials = variable.getEffectiveMaterials();

    if (!materials.isEmpty()) {
      if (materials.stream().noneMatch(material -> material.equals(state.item.getType())))
        return this;
    }

    return null;
  }

  @Override
  public void stringify(StringifyHandler handler) {
    handler.stringify(this, output -> {
      if (token != null && (handler.useTokens() || translatedLangKeyed == null))
        output.appendString(token.stringify());
      else
        output.appendString(translatedLangKeyed.normalizedPrefixedTranslation);
    });
  }

  @Override
  public boolean isTransitiveParentTo(ItemPredicate node) {
    return false;
  }
}
