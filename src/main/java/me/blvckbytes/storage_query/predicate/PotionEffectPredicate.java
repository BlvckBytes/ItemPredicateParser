package me.blvckbytes.storage_query.predicate;

import me.blvckbytes.storage_query.token.IntegerToken;
import me.blvckbytes.storage_query.translation.TranslatedTranslatable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.util.StringJoiner;

public record PotionEffectPredicate(
  TranslatedTranslatable translatedTranslatable,
  PotionEffectType type,
  @Nullable IntegerToken amplifierArgument,
  @Nullable IntegerToken durationArgument
) implements ItemPredicate {

  @Override
  public boolean test(ItemStack item) {
    var meta = item.getItemMeta();

    if (!(meta instanceof PotionMeta potionMeta))
      return false;

    var baseType = potionMeta.getBasePotionType();

    if (baseType != null) {
      for (var baseEffect : baseType.getPotionEffects()) {
        if (!baseEffect.getType().equals(this.type))
          continue;

        if (this.amplifierArgument != null && this.amplifierArgument.matches(baseEffect.getAmplifier()))
          continue;

        if (this.durationArgument != null && this.durationArgument.matches(baseEffect.getAmplifier()))
          continue;

        return true;
      }
    }

    for (var customEffect : potionMeta.getCustomEffects()) {
      if (!customEffect.getType().equals(this.type))
        continue;

      if (this.amplifierArgument != null && this.amplifierArgument.matches(customEffect.getAmplifier()))
        continue;

      if (this.durationArgument != null && this.durationArgument.matches(customEffect.getAmplifier()))
        continue;

      return true;
    }

    return false;
  }

  @Override
  public String stringify() {
    var result = new StringJoiner(" ");

    result.add(translatedTranslatable.normalizedName());

    if (this.amplifierArgument != null)
      result.add(this.amplifierArgument.stringify());

    if (this.durationArgument != null)
      result.add(this.durationArgument.stringify());

    return result.toString();
  }
}
