package me.blvckbytes.item_predicate_parser.predicate;

import me.blvckbytes.item_predicate_parser.token.Token;
import me.blvckbytes.item_predicate_parser.translation.TranslatedLangKeyed;
import me.blvckbytes.item_predicate_parser.translation.keyed.LangKeyedMusicInstrument;
import org.bukkit.inventory.meta.MusicInstrumentMeta;
import org.jetbrains.annotations.Nullable;

public record MusicInstrumentPredicate(
  Token token,
  TranslatedLangKeyed<LangKeyedMusicInstrument> translatedLangKeyed
) implements ItemPredicate {

  @Override
  public @Nullable ItemPredicate testForFailure(PredicateState state) {
    if (!(state.meta instanceof MusicInstrumentMeta instrumentMeta))
      return this;

    if (translatedLangKeyed.langKeyed.getWrapped().equals(instrumentMeta.getInstrument()))
      return null;

    return this;
  }

  @Override
  public String stringify(boolean useTokens) {
    if (useTokens)
      return token.stringify();
    return translatedLangKeyed.normalizedPrefixedTranslation;
  }
}
