package me.blvckbytes.storage_query.predicate;

import me.blvckbytes.storage_query.token.IntegerToken;
import me.blvckbytes.storage_query.translation.TranslatedTranslatable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.jetbrains.annotations.Nullable;

import java.util.StringJoiner;

public record DeteriorationPredicate(
  TranslatedTranslatable translatedTranslatable,
  @Nullable IntegerToken deteriorationPercentageMin,
  @Nullable IntegerToken deteriorationPercentageMax
) implements ItemPredicate {

  @Override
  public boolean test(ItemStack item) {
    if (item.getItemMeta() instanceof Damageable damageableMeta) {
      var damage = damageableMeta.getDamage();
      var maxDamage = (int) item.getType().getMaxDurability();

      if (maxDamage == 0)
        return false;

      var deteriorationPercentage = Math.round(((double) damage / maxDamage) * 100.0);

      if (this.deteriorationPercentageMin != null) {
        if (deteriorationPercentageMin.value() != null) {
          if (!(deteriorationPercentage >= deteriorationPercentageMin.value()))
            return false;
        }
      }

      if (this.deteriorationPercentageMax != null) {
        if (deteriorationPercentageMax.value() != null)
          return deteriorationPercentage <= deteriorationPercentageMax.value();
      }

      return true;
    }

    return false;
  }

  @Override
  public String stringify() {
    var result = new StringJoiner(" ");

    result.add(translatedTranslatable.normalizedName());

    if (deteriorationPercentageMin != null)
      result.add(deteriorationPercentageMin.stringify());

    if (deteriorationPercentageMax != null)
      result.add(deteriorationPercentageMax.stringify());

    return result.toString();
  }
}
