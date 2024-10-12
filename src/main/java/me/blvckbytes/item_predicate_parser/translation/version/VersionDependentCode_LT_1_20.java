package me.blvckbytes.item_predicate_parser.translation.version;

import com.google.gson.JsonObject;
import me.blvckbytes.item_predicate_parser.predicate.ItemPredicate;
import me.blvckbytes.item_predicate_parser.token.Token;
import me.blvckbytes.item_predicate_parser.translation.TranslatedLangKeyed;
import me.blvckbytes.item_predicate_parser.translation.keyed.*;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.logging.Logger;

public class VersionDependentCode_LT_1_20 implements IVersionDependentCode {

  private final Logger logger;
  private final DetectedServerVersion serverVersion;

  public VersionDependentCode_LT_1_20(DetectedServerVersion serverVersion, Logger logger) {
    this.logger = logger;
    this.serverVersion = serverVersion;
  }

  @Override
  @SuppressWarnings("deprecation")
  public Iterable<? extends LangKeyed<?>> getEnchantments() {
    return Arrays.stream(Enchantment.values()).map(LangKeyedEnchantment::new).toList();
  }

  @Override
  @SuppressWarnings("deprecation")
  public Iterable<? extends LangKeyed<?>> getEffects() {
    return Arrays.stream(PotionEffectType.values()).map(LangKeyedPotionEffectType::new).toList();
  }

  @Override
  public Iterable<? extends LangKeyed<?>> getItemMaterials(JsonObject languageJson) {
    return Arrays.stream(Material.values()).filter(Material::isItem).map(it -> new LangKeyedItemMaterial(it, serverVersion, languageJson)).toList();
  }

  @Override
  public @Nullable Iterable<? extends LangKeyed<?>> getInstruments() {
    return null;
  }

  @Override
  public @Nullable ItemPredicate makeInstrumentPredicate(Token token, TranslatedLangKeyed<?> translation) {
    logger.severe("Tried to instantiate a MusicInstrument-based predicate on the unsupported version " + serverVersion.original());
    return null;
  }
}
