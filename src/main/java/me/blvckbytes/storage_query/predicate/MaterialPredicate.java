package me.blvckbytes.storage_query.predicate;

import me.blvckbytes.storage_query.token.Token;
import me.blvckbytes.storage_query.translation.TranslatedTranslatable;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

public record MaterialPredicate(
  Token token,
  @Nullable TranslatedTranslatable translatedTranslatable,
  List<Material> materials
) implements ItemPredicate {

  @Override
  public boolean test(ItemStack item, EnumSet<PredicateFlags> flags) {
    for (var material : materials) {
      if (material.equals(item.getType()))
        return true;
    }
    return false;
  }

  @Override
  public String stringify(boolean useTokens) {
    if (translatedTranslatable != null && !useTokens)
      return translatedTranslatable.normalizedName();

    return token.stringify();
  }
}
