package me.blvckbytes.storage_query.predicate;

import me.blvckbytes.storage_query.token.Token;
import me.blvckbytes.storage_query.translation.TranslatedTranslatable;
import org.bukkit.inventory.ItemStack;

import java.util.EnumSet;

public record ExactNode(
  Token token,
  TranslatedTranslatable translatedTranslatable,
  ItemPredicate operand
) implements ItemPredicate {

  @Override
  public boolean test(ItemStack item, EnumSet<PredicateFlags> flags) {
    if (flags.contains(PredicateFlags.EXACT_MODE)) {
      // TODO: Implement correct behavior
    }

    return operand.test(item, flags);
  }

  @Override
  public String stringify(boolean useTokens) {
    if (useTokens)
      return token.stringify() + " " + operand.stringify(true);

    return translatedTranslatable.normalizedName() + " " + operand.stringify(false);
  }
}
