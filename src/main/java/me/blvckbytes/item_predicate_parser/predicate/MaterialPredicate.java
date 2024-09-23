package me.blvckbytes.item_predicate_parser.predicate;

import me.blvckbytes.item_predicate_parser.token.Token;
import me.blvckbytes.item_predicate_parser.translation.TranslatedLangKeyed;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record MaterialPredicate(
  Token token,
  @Nullable TranslatedLangKeyed<?> translatedLangKeyed,
  List<Material> materials
) implements ItemPredicate {

  @Override
  public boolean test(PredicateState state) {
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
