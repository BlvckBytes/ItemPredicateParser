package me.blvckbytes.item_predicate_parser.predicate;

import me.blvckbytes.item_predicate_parser.predicate.stringify.StringifyHandler;
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
  public void stringify(StringifyHandler handler) {
    handler.stringify(this, output -> {
      if (handler.useTokens() || translatedLangKeyed == null)
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
