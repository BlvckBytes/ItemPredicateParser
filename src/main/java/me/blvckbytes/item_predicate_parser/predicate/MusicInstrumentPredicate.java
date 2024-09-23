package me.blvckbytes.item_predicate_parser.predicate;

import me.blvckbytes.item_predicate_parser.token.Token;
import me.blvckbytes.item_predicate_parser.translation.TranslatedLangKeyed;
import org.bukkit.MusicInstrument;
import org.bukkit.inventory.meta.MusicInstrumentMeta;

public record MusicInstrumentPredicate(
  Token token,
  TranslatedLangKeyed translatedLangKeyed,
  MusicInstrument instrument
) implements ItemPredicate {

  @Override
  public boolean test(PredicateState state) {
    if (!(state.meta instanceof MusicInstrumentMeta instrumentMeta))
      return false;

    return instrument.equals(instrumentMeta.getInstrument());
  }

  @Override
  public String stringify(boolean useTokens) {
    if (useTokens)
      return token.stringify();
    return translatedLangKeyed.normalizedPrefixedTranslation;
  }
}
