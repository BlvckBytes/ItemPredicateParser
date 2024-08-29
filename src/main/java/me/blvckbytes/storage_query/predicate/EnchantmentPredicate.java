package me.blvckbytes.storage_query.predicate;

import me.blvckbytes.storage_query.token.IntegerToken;
import me.blvckbytes.storage_query.token.Token;
import me.blvckbytes.storage_query.translation.TranslatedTranslatable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
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
    for (var entry : extractEnchantments(state.meta, false)) {
      if (doesEnchantmentMatch(entry.getKey(), entry.getValue()))
        return true;
    }

    return false;
  }

  public Set<Map.Entry<Enchantment, Integer>> extractEnchantments(@Nullable ItemMeta meta, boolean modifiable) {
    if (meta == null)
      return Set.of();

    Map<Enchantment, Integer> enchantments;

    if (meta instanceof EnchantmentStorageMeta enchantmentStorageMeta)
      enchantments = enchantmentStorageMeta.getStoredEnchants();
    else
      enchantments = meta.getEnchants();

    if (!modifiable)
      return enchantments.entrySet();

    return new HashSet<>(enchantments.entrySet());
  }

  private boolean doesEnchantmentMatch(Enchantment enchantment, int level) {
    if (!enchantment.equals(this.enchantment))
      return false;

    return this.levelArgument == null || this.levelArgument.matches(level);
  }

  public boolean matchOnRemaining(ItemStack item, Set<Map.Entry<Enchantment, Integer>> remaining) {
    for (var entryIterator = remaining.iterator(); entryIterator.hasNext();) {
      var entry = entryIterator.next();

      if (doesEnchantmentMatch(entry.getKey(), entry.getValue())) {
        entryIterator.remove();
        return true;
      }
    }

    return false;
  }

  @Override
  public String stringify(boolean useTokens) {
    var result = new StringJoiner(" ");

    if (useTokens)
      result.add(token.stringify());
    else
      result.add(translatedTranslatable.normalizedName());

    if (this.levelArgument != null)
      result.add(this.levelArgument.stringify());

    return result.toString();
  }
}
