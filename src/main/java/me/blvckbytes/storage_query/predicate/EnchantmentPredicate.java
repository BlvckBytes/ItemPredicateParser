package me.blvckbytes.storage_query.predicate;

import me.blvckbytes.storage_query.argument.IntegerArgument;
import me.blvckbytes.storage_query.translation.TranslatedTranslatable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.StringJoiner;

public record EnchantmentPredicate(
  TranslatedTranslatable translatedTranslatable,
  Enchantment enchantment,
  @Nullable IntegerArgument levelArgument
) implements ItemPredicate {

  @Override
  public boolean test(ItemStack item) {
    ItemMeta meta = item.getItemMeta();

    if (meta == null)
      return false;

    for (var enchant : meta.getEnchants().entrySet()) {
      if (!enchant.getKey().equals(this.enchantment))
        continue;

      if (this.levelArgument != null && !this.levelArgument.matches(enchant.getValue()))
        continue;

      return true;
    }

    return false;
  }

  @Override
  public String stringify() {
    var result = new StringJoiner(" ");

    result.add(translatedTranslatable.normalizedName());

    if (this.levelArgument != null)
      result.add(this.levelArgument.stringify());

    return result.toString();
  }
}
