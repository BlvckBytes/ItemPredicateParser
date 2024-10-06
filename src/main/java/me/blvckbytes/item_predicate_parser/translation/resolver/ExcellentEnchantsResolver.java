package me.blvckbytes.item_predicate_parser.translation.resolver;

import me.blvckbytes.item_predicate_parser.translation.keyed.LangKeyed;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.excellentenchants.registry.EnchantRegistry;

public class ExcellentEnchantsResolver extends TranslationResolver {

  public ExcellentEnchantsResolver(Plugin loadedPlugin) {
    super(loadedPlugin);
  }

  @Override
  public @Nullable String resolve(LangKeyed<?> langKeyed) {
    if (!(langKeyed.getWrapped() instanceof Enchantment enchantment))
      return null;

    var customEnchantment = EnchantRegistry.getByKey(enchantment.getKey());

    if (customEnchantment == null)
      return null;

    return sanitize(customEnchantment.getDisplayName());
  }
}
