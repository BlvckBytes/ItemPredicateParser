package me.blvckbytes.item_predicate_parser.predicate;

import me.blvckbytes.item_predicate_parser.predicate.stringify.StringifyHandler;
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
    if (!(state.getMeta() instanceof MusicInstrumentMeta instrumentMeta))
      return this;

    if (translatedLangKeyed.langKeyed.getWrapped().equals(instrumentMeta.getInstrument()))
      return null;

    return this;
  }

  @Override
  public void stringify(StringifyHandler handler) {
    handler.stringify(this, output -> {
      if (handler.useTokens())
        output.appendString(token.stringify());
      else
        output.appendString(translatedLangKeyed.normalizedPrefixedTranslation);
    });
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof MusicInstrumentPredicate otherPredicate))
      return false;

    return this.translatedLangKeyed.equals(otherPredicate.translatedLangKeyed);
  }
}
