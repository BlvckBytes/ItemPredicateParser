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
  public boolean test(PredicateState state) {
    if (translatedLangKeyed != null)
      return translatedLangKeyed.langKeyed.getWrapped().equals(state.item.getType());

    if (materials == null)
      return true;

    for (var material : materials) {
      if (material.equals(state.item.getType()))
        return true;
    }
    return false;
  }

  @Override
  public String stringify(boolean useTokens) {
    if (translatedLangKeyed != null && !useTokens)
      return translatedLangKeyed.normalizedPrefixedTranslation;

    return token.stringify();
  }
}
