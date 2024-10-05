package me.blvckbytes.item_predicate_parser.predicate;

import me.blvckbytes.item_predicate_parser.token.Token;
import me.blvckbytes.item_predicate_parser.translation.TranslatedLangKeyed;
import me.blvckbytes.item_predicate_parser.translation.keyed.LangKeyedItemMaterial;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record MaterialPredicate(
  Token token,
  @Nullable TranslatedLangKeyed<LangKeyedItemMaterial> translatedLangKeyed,
  @Nullable List<Material> materials
) implements ItemPredicate {

  @Override
  public @Nullable ItemPredicate testForFailure(PredicateState state) {
    if (translatedLangKeyed != null) {
      if (translatedLangKeyed.langKeyed.getWrapped().equals(state.item.getType()))
        return null;

      return this;
    }

    if (materials == null)
      return this;

    for (var material : materials) {
      if (material.equals(state.item.getType()))
        return null;
    }

    return this;
  }

  @Override
  public void stringify(StringifyState state) {
    if (state.useTokens || translatedLangKeyed == null)
      state.appendString(token.stringify());
    else
      state.appendString(translatedLangKeyed.normalizedPrefixedTranslation);
  }
}
