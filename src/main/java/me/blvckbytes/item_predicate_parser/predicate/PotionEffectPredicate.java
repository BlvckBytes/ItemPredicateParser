package me.blvckbytes.item_predicate_parser.predicate;

import me.blvckbytes.item_predicate_parser.token.IntegerToken;
import me.blvckbytes.item_predicate_parser.token.Token;
import me.blvckbytes.item_predicate_parser.translation.TranslatedLangKeyed;
import me.blvckbytes.item_predicate_parser.translation.keyed.LangKeyedPotionEffectType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public record PotionEffectPredicate(
  Token token,
  TranslatedLangKeyed<LangKeyedPotionEffectType> translatedLangKeyed,
  @Nullable IntegerToken amplifierArgument,
  @Nullable IntegerToken durationArgument
) implements ItemPredicate {

  @Override
  public @Nullable ItemPredicate testForFailure(PredicateState state) {
    for (var effectIterator = state.getEffects().iterator(); effectIterator.hasNext();) {
      var effect = effectIterator.next();

      if (!effect.getType().equals(this.translatedLangKeyed.langKeyed.getWrapped()))
        continue;

      if (doesAmplifierMismatch(effect.getAmplifier()))
        continue;

      if (doesDurationMismatch(state.item, effect.getDuration()))
        continue;

      if (state.isExactMode)
        effectIterator.remove();

      return null;
    }

    return this;
  }

  @Override
  public void stringify(StringifyState state) {
    if (state.useTokens)
      state.appendString(token.stringify());
    else
      state.appendString(translatedLangKeyed.normalizedPrefixedTranslation);

    if (this.amplifierArgument != null) {
      state.appendSpace();
      state.appendString(this.amplifierArgument.stringify());
    }

    if (this.durationArgument != null) {
      state.appendSpace();
      state.appendString(this.durationArgument.stringify());
    }
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
