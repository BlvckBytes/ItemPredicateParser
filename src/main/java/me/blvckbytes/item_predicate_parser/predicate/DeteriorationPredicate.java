package me.blvckbytes.item_predicate_parser.predicate;

import me.blvckbytes.item_predicate_parser.token.IntegerToken;
import me.blvckbytes.item_predicate_parser.token.Token;
import me.blvckbytes.item_predicate_parser.translation.keyed.DeteriorationKey;
import me.blvckbytes.item_predicate_parser.translation.TranslatedLangKeyed;
import org.bukkit.inventory.meta.Damageable;
import org.jetbrains.annotations.Nullable;

import java.util.StringJoiner;

public record DeteriorationPredicate(
  Token token,
  TranslatedLangKeyed<DeteriorationKey> translatedLangKeyed,
  @Nullable IntegerToken deteriorationPercentageMin,
  @Nullable IntegerToken deteriorationPercentageMax
) implements ItemPredicate {

  @Override
  public @Nullable ItemPredicate testForFailure(PredicateState state) {
    if (!(state.meta instanceof Damageable damageableMeta))
      return this;

    var damage = damageableMeta.getDamage();
    var maxDamage = (int) state.item.getType().getMaxDurability();

    if (maxDamage == 0)
      return this;

    var deteriorationPercentage = Math.round(((double) damage / maxDamage) * 100.0);

    if (this.deteriorationPercentageMin != null && this.deteriorationPercentageMin.value() != null) {
      if (!(deteriorationPercentage >= deteriorationPercentageMin.value()))
        return this;
    }

    if (this.deteriorationPercentageMax != null && deteriorationPercentageMax.value() != null) {
      if (!(deteriorationPercentage <= deteriorationPercentageMax.value()))
        return this;
    }

    return null;
  }

  @Override
  public String stringify(boolean useTokens) {
    var result = new StringJoiner(" ");

    if (useTokens)
      result.add(token.stringify());
    else
      result.add(translatedLangKeyed.normalizedPrefixedTranslation);

    if (deteriorationPercentageMin != null)
      result.add(deteriorationPercentageMin.stringify());

    if (deteriorationPercentageMax != null)
      result.add(deteriorationPercentageMax.stringify());

    return result.toString();
  }
}
