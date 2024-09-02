package me.blvckbytes.item_predicate_parser.predicate;

import me.blvckbytes.item_predicate_parser.token.IntegerToken;
import me.blvckbytes.item_predicate_parser.token.Token;
import me.blvckbytes.item_predicate_parser.translation.TranslatedTranslatable;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public record EnchantmentPredicate(
  Token token,
  TranslatedTranslatable translatedTranslatable,
  Enchantment enchantment,
  @Nullable IntegerToken levelArgument
) implements ItemPredicate {

  @Override
  public boolean test(PredicateState state) {
    for (var entryIterator = state.getEnchantments().iterator(); entryIterator.hasNext();) {
      var entry = entryIterator.next();

      if (doesEnchantmentMatch(entry.getKey(), entry.getValue())) {
        if (state.isExactMode)
          entryIterator.remove();

        return true;
      }
    }

    return false;
  }

  private boolean doesEnchantmentMatch(Enchantment enchantment, int level) {
    if (!enchantment.equals(this.enchantment))
      return false;

    return this.levelArgument == null || this.levelArgument.matches(level);
  }

  @Override
  public String stringify(boolean useTokens) {
    var result = new StringJoiner(" ");

    if (useTokens)
      result.add(token.stringify());
    else
      result.add(translatedTranslatable.normalizedName);

    if (this.levelArgument != null)
      result.add(this.levelArgument.stringify());

    return result.toString();
  }
}
