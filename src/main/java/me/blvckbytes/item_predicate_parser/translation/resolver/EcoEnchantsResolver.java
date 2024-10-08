package me.blvckbytes.item_predicate_parser.translation.resolver;

import com.willfp.ecoenchants.enchant.EcoEnchants;
import me.blvckbytes.item_predicate_parser.translation.keyed.LangKeyed;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

public class EcoEnchantsResolver extends TranslationResolver {

  public EcoEnchantsResolver(Plugin loadedPlugin) {
    super(loadedPlugin);
  }

  @Override
  public @Nullable String resolve(LangKeyed<?> langKeyed) {
    if (!(langKeyed.getWrapped() instanceof Enchantment enchantment))
      return null;

    var id = enchantment.getKey().getKey();
    var ecoEnchant = EcoEnchants.INSTANCE.get(id);

    if (ecoEnchant == null)
      return null;

    return sanitize(ecoEnchant.getRawDisplayName());
  }
}
