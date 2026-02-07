package me.blvckbytes.item_predicate_parser.predicate;

import me.blvckbytes.item_predicate_parser.predicate.stringify.StringifyHandler;
import me.blvckbytes.item_predicate_parser.token.IntegerToken;
import me.blvckbytes.item_predicate_parser.token.Token;
import me.blvckbytes.item_predicate_parser.translation.keyed.DeteriorationKey;
import me.blvckbytes.item_predicate_parser.translation.TranslatedLangKeyed;
import org.bukkit.inventory.meta.Damageable;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Objects;

public record DeteriorationPredicate(
  Token token,
  TranslatedLangKeyed<DeteriorationKey> translatedLangKeyed,
  @Nullable IntegerToken deteriorationPercentageMin,
  @Nullable IntegerToken deteriorationPercentageMax
) implements ItemPredicate {

  @Override
  public @Nullable ItemPredicate testForFailure(PredicateState state) {
    if (!(state.getMeta() instanceof Damageable damageableMeta))
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
  public void stringify(StringifyHandler handler) {
    handler.stringify(this, output -> {
      if (handler.useTokens())
        output.appendString(token.stringify());
      else
        output.appendString(translatedLangKeyed.normalizedPrefixedTranslation);

      if (deteriorationPercentageMin != null) {
        output.appendSpace();
        output.appendString(deteriorationPercentageMin.stringify());
      }

      if (deteriorationPercentageMax != null) {
        output.appendSpace();
        output.appendString(deteriorationPercentageMax.stringify());
      }
    });
  }

  @Override
  public boolean containsOrEqualsPredicate(ItemPredicate node, EnumSet<ComparisonFlag> comparisonFlags) {
    return equals(node);
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof DeteriorationPredicate otherPredicate))
      return false;

    if (!Objects.equals(this.deteriorationPercentageMin, otherPredicate.deteriorationPercentageMin))
      return false;

    return Objects.equals(this.deteriorationPercentageMax, otherPredicate.deteriorationPercentageMax);
  }
}
