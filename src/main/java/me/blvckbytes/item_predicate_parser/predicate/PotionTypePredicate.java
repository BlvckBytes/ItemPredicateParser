package me.blvckbytes.item_predicate_parser.predicate;

import me.blvckbytes.item_predicate_parser.token.Token;
import me.blvckbytes.item_predicate_parser.translation.TranslatedLangKeyed;
import me.blvckbytes.item_predicate_parser.translation.keyed.LangKeyedPotionType;
import org.bukkit.inventory.meta.PotionMeta;
import org.jetbrains.annotations.Nullable;

public record PotionTypePredicate(
  Token token,
  TranslatedLangKeyed<LangKeyedPotionType> translatedLangKeyed
) implements ItemPredicate {

  @Override
  public @Nullable ItemPredicate testForFailure(PredicateState state) {
    if (!(state.getMeta() instanceof PotionMeta potionMeta))
      return this;

    if (!this.translatedLangKeyed.langKeyed.getWrapped().equals(potionMeta.getBasePotionType()))
      return this;

    return null;
  }

  @Override
  public void stringify(StringifyState state) {
    if (state.useTokens)
      state.appendString(token.stringify());
    else
      state.appendString(translatedLangKeyed.normalizedPrefixedTranslation);
  }

  @Override
  public boolean isTransitiveParentTo(ItemPredicate node) {
    return false;
  }
}
