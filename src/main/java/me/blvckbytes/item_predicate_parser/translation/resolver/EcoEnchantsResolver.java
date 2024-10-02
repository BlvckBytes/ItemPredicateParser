package me.blvckbytes.item_predicate_parser.translation.resolver;

import com.willfp.ecoenchants.enchant.EcoEnchants;
import me.blvckbytes.item_predicate_parser.translation.keyed.LangKeyed;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.Nullable;

public class EcoEnchantsResolver extends TranslationResolver {

  @Override
  public @Nullable String resolve(LangKeyed<?> langKeyed) {

    if (langKeyed.getWrapped() instanceof Enchantment enchantment) {
      var id = enchantment.getKey().getKey();
      var ecoEnchant = EcoEnchants.INSTANCE.get(id);

      if (ecoEnchant == null)
        return null;

      return sanitize(ecoEnchant.getRawDisplayName());
    }

    return null;
  }
}
