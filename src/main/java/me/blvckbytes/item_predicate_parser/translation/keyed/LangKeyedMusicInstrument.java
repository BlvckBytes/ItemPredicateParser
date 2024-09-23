package me.blvckbytes.item_predicate_parser.translation.keyed;

import org.bukkit.MusicInstrument;

import java.util.Objects;

public class LangKeyedMusicInstrument implements LangKeyed<MusicInstrument> {

  private final MusicInstrument instrument;
  private final String languageFileKey;

  public LangKeyedMusicInstrument(MusicInstrument instrument) {
    this.instrument = instrument;

    var namespacedKey = instrument.getKey();
    this.languageFileKey = "instrument." + namespacedKey.getNamespace() + "." + namespacedKey.getKey();
  }

  @Override
  public String getLanguageFileKey() {
    return languageFileKey;
  }

  @Override
  public MusicInstrument getWrapped() {
    return instrument;
  }

  @Override
  public LangKeyedPredicateType getPredicateType() {
    return LangKeyedPredicateType.MUSIC_INSTRUMENT;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof LangKeyedMusicInstrument that)) return false;
    return Objects.equals(instrument, that.instrument);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(instrument);
  }

  @Override
  public String toString() {
    return instrument.toString();
  }
}
