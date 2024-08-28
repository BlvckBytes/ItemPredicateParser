package me.blvckbytes.storage_query.predicate;

import me.blvckbytes.storage_query.token.IntegerToken;
import me.blvckbytes.storage_query.token.Token;
import me.blvckbytes.storage_query.translation.TranslatedTranslatable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Map;
import java.util.StringJoiner;

public record EnchantmentPredicate(
  Token token,
  TranslatedTranslatable translatedTranslatable,
  Enchantment enchantment,
  @Nullable IntegerToken levelArgument
) implements ItemPredicate {

  @Override
  public boolean test(ItemStack item, EnumSet<PredicateFlags> flags) {
    ItemMeta meta = item.getItemMeta();

    if (meta == null)
      return false;

    Map<Enchantment, Integer> enchantments;

    if (meta instanceof EnchantmentStorageMeta enchantmentStorage)
      enchantments = enchantmentStorage.getStoredEnchants();
    else
      enchantments = meta.getEnchants();

    for (var enchant : enchantments.entrySet()) {
      if (!enchant.getKey().equals(this.enchantment))
        continue;

      if (this.levelArgument != null && !this.levelArgument.matches(enchant.getValue()))
        continue;

      return true;
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
