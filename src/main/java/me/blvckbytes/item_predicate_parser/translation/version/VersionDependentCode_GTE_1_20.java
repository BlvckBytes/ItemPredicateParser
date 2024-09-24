package me.blvckbytes.item_predicate_parser.translation.version;

import com.google.gson.JsonObject;
import me.blvckbytes.item_predicate_parser.predicate.ItemPredicate;
import me.blvckbytes.item_predicate_parser.predicate.MusicInstrumentPredicate;
import me.blvckbytes.item_predicate_parser.token.Token;
import me.blvckbytes.item_predicate_parser.translation.TranslatedLangKeyed;
import me.blvckbytes.item_predicate_parser.translation.keyed.*;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Logger;

public class VersionDependentCode_GTE_1_20 implements IVersionDependentCode {

  private final Logger logger;

  public VersionDependentCode_GTE_1_20(DetectedServerVersion ignored, Logger logger) {
    this.logger = logger;
  }

  @Override
  public Iterable<? extends LangKeyed<?>> getEnchantments() {
    return Registry.ENCHANTMENT.stream().map(LangKeyedEnchantment::new).toList();
  }

  @Override
  public Iterable<? extends LangKeyed<?>> getEffects() {
    return Registry.EFFECT.stream().map(LangKeyedPotionEffectType::new).toList();
  }

  @Override
  public Iterable<? extends LangKeyed<?>> getItemMaterials(JsonObject languageJson) {
    return Registry.MATERIAL.stream().filter(Material::isItem).map(it -> new LangKeyedItemMaterial(it, languageJson)).toList();
  }

  @Override
  public @Nullable Iterable<? extends LangKeyed<?>> getInstruments() {
    return Registry.INSTRUMENT.stream().map(LangKeyedMusicInstrument::new).toList();
  }

  @Override
  @SuppressWarnings("unchecked")
  public @Nullable ItemPredicate makeInstrumentPredicate(Token token, TranslatedLangKeyed<?> translation) {
    if (!(translation.langKeyed instanceof LangKeyedMusicInstrument)) {
      logger.severe("Unexpected lang-keyed of non-MusicInstrument type; returning null!");
      return null;
    }

    return new MusicInstrumentPredicate(token, (TranslatedLangKeyed<LangKeyedMusicInstrument>) translation);
  }
}
