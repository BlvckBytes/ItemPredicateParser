package me.blvckbytes.storage_query.predicate;

import me.blvckbytes.storage_query.token.IntegerToken;
import me.blvckbytes.storage_query.token.Token;
import me.blvckbytes.storage_query.translation.TranslatedTranslatable;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.StringJoiner;

public record PotionEffectPredicate(
  Token token,
  TranslatedTranslatable translatedTranslatable,
  PotionEffectType type,
  @Nullable IntegerToken amplifierArgument,
  @Nullable IntegerToken durationArgument
) implements ItemPredicate {

  @Override
  public boolean test(ItemStack item, @Nullable ItemMeta meta, EnumSet<PredicateFlags> flags) {
    if (!(meta instanceof PotionMeta potionMeta))
      return false;

    var baseType = potionMeta.getBasePotionType();

    if (baseType != null) {
      for (var baseEffect : baseType.getPotionEffects()) {
        if (!baseEffect.getType().equals(this.type))
          continue;

        if (doesAmplifierMismatch(baseEffect.getAmplifier()))
          continue;

        if (doesDurationMismatch(item, baseEffect.getDuration()))
          continue;

        return true;
      }
    }

    for (var customEffect : potionMeta.getCustomEffects()) {
      if (!customEffect.getType().equals(this.type))
        continue;

      if (doesAmplifierMismatch(customEffect.getAmplifier()))
        continue;

      if (doesDurationMismatch(item, customEffect.getDuration()))
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

    if (this.amplifierArgument != null)
      result.add(this.amplifierArgument.stringify());

    if (this.durationArgument != null)
      result.add(this.durationArgument.stringify());

    return result.toString();
  }

  private boolean doesAmplifierMismatch(int amplifier) {
    if (this.amplifierArgument == null)
      return false;

    // Amplifiers are stored in a zero-based manner
    return !this.amplifierArgument.matches(amplifier + 1);
  }

  private boolean doesDurationMismatch(ItemStack item, int duration) {
    if (this.durationArgument == null)
      return false;

    // Lingering Potion: "For effects with duration, the duration applied by the
    // cloud is 1/4 that of the corresponding potion."
    if (item.getType() == Material.LINGERING_POTION)
      duration /= 4;

    // The unit of duration is always in ticks, with 20 ticks/s
    return !this.durationArgument.matches(duration / 20);
  }
}
