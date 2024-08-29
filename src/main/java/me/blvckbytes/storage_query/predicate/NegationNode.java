package me.blvckbytes.storage_query.predicate;

import me.blvckbytes.storage_query.token.Token;
import me.blvckbytes.storage_query.translation.TranslatedTranslatable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public record NegationNode(
  Token token,
  TranslatedTranslatable translatedTranslatable,
  ItemPredicate operand
) implements ItemPredicate {

  @Override
  public boolean test(ItemStack item, @Nullable ItemMeta meta, EnumSet<PredicateFlags> flags) {
    return !operand.test(item, meta, flags);
  }

  @Override
  public String stringify(boolean useTokens) {
    if (useTokens)
      return token.stringify() + " " + operand.stringify(true);

    return translatedTranslatable.normalizedName() + " " + operand.stringify(false);
  }
}
