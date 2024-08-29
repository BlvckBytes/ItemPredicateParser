package me.blvckbytes.storage_query.predicate;

import me.blvckbytes.storage_query.token.Token;
import me.blvckbytes.storage_query.translation.TranslatedTranslatable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public record DisjunctionNode(
  Token token,
  TranslatedTranslatable translatedTranslatable,
  ItemPredicate lhs,
  ItemPredicate rhs
) implements ItemPredicate {

  @Override
  public boolean test(ItemStack item, @Nullable ItemMeta meta, EnumSet<PredicateFlags> flags) {
    return lhs.test(item, meta, flags) || rhs.test(item, meta, flags);
  }

  @Override
  public String stringify(boolean useTokens) {
    if (useTokens)
      return lhs.stringify(true) + " " + token.stringify() + " " + rhs.stringify(true);

    return lhs.stringify(false) + " " + translatedTranslatable.normalizedName() + " " + rhs.stringify(false);
  }
}
