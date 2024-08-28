package me.blvckbytes.storage_query.predicate;

import me.blvckbytes.storage_query.token.Token;
import me.blvckbytes.storage_query.translation.TranslatedTranslatable;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public record ConjunctionNode(
  @Nullable Token token,
  TranslatedTranslatable translatedTranslatable,
  ItemPredicate lhs,
  ItemPredicate rhs,
  boolean wasImplicit
) implements ItemPredicate {

  @Override
  public boolean test(ItemStack item) {
    return lhs.test(item) && rhs.test(item);
  }

  @Override
  public String stringify(boolean useTokens) {
    if (wasImplicit)
      return lhs.stringify(useTokens) + " " + rhs.stringify(useTokens);

    if (useTokens && token != null)
      return lhs.stringify(true) + " " + token.stringify() + " " + rhs.stringify(true);

    return lhs.stringify(useTokens) + " " + translatedTranslatable.normalizedName() + " " + rhs.stringify(useTokens);
  }
}
