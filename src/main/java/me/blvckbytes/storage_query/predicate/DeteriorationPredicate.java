package me.blvckbytes.storage_query.predicate;

import me.blvckbytes.storage_query.token.IntegerToken;
import me.blvckbytes.storage_query.translation.TranslatedTranslatable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

public record DeteriorationPredicate(
  TranslatedTranslatable translatedTranslatable,
  IntegerToken deteriorationPercentageMin,
  IntegerToken deteriorationPercentageMax
) implements ItemPredicate {

  @Override
  public boolean test(ItemStack item) {
    if (item.getItemMeta() instanceof Damageable damageableMeta) {
      var damage = damageableMeta.getDamage();
      var maxDamage = (int) item.getType().getMaxDurability();

      if (maxDamage == 0)
        return false;

      var deteriorationPercentage = Math.round(((double) damage / maxDamage) * 100.0);

      var percentageMin = deteriorationPercentageMin.value();
      var percentageMax = deteriorationPercentageMax.value();

      if (percentageMin != null) {
        if (!(deteriorationPercentage >= percentageMin))
          return false;
      }

      if (percentageMax != null)
        return deteriorationPercentage <= percentageMax;

      return true;
    }

    return false;
  }

  @Override
  public String stringify() {
    return translatedTranslatable.normalizedName() + " " + deteriorationPercentageMin.stringify() + " " + deteriorationPercentageMax.stringify();
  }
}
