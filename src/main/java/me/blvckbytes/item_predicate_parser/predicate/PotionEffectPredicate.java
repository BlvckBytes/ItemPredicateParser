package me.blvckbytes.item_predicate_parser.predicate;

import me.blvckbytes.item_predicate_parser.token.IntegerToken;
import me.blvckbytes.item_predicate_parser.token.Token;
import me.blvckbytes.item_predicate_parser.translation.TranslatedTranslatable;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.util.StringJoiner;

public record PotionEffectPredicate(
  Token token,
  TranslatedTranslatable translatedTranslatable,
  PotionEffectType type,
  @Nullable IntegerToken amplifierArgument,
  @Nullable IntegerToken durationArgument
) implements ItemPredicate {

  @Override
  public boolean test(PredicateState state) {
    for (var effectIterator = state.getEffects().iterator(); effectIterator.hasNext();) {
      var effect = effectIterator.next();

      if (!effect.getType().equals(this.type))
        continue;

      if (doesAmplifierMismatch(effect.getAmplifier()))
        continue;

      if (doesDurationMismatch(state.item, effect.getDuration()))
        continue;

      if (state.isExactMode)
        effectIterator.remove();

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
      result.add(translatedTranslatable.normalizedTranslation);

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
