package me.blvckbytes.storage_query;

import me.blvckbytes.storage_query.translation.*;
import org.bukkit.Bukkit;
import org.bukkit.Registry;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;

public class StorageQueryPlugin extends JavaPlugin {

  @Override
  public void onEnable() {
    var logger = getLogger();

    var customSource = new TranslatableSource(List.of(
      DeteriorationKey.INSTANCE,
      NegationKey.INSTANCE,
      DisjunctionKey.INSTANCE,
      ConjunctionKey.INSTANCE,
      ExactKey.INSTANCE,
      AmountKey.INSTANCE
    ), "");

    var germanSources = Arrays.asList(
      new TranslatableSource(Registry.ENCHANTMENT, "[Verzauberung] "),
      new TranslatableSource(Registry.EFFECT, "[Effekt] "),
      new TranslatableSource(Registry.MATERIAL, "[Typ] "),
      customSource
    );

    var englishSources = Arrays.asList(
      new TranslatableSource(Registry.ENCHANTMENT, "[Enchantment] "),
      new TranslatableSource(Registry.EFFECT, "[Effect] "),
      new TranslatableSource(Registry.MATERIAL, "[Material] "),
      customSource
    );

    var registryGerman = TranslationRegistry.load("/de_de.json", germanSources, logger);
    var registryEnglish = TranslationRegistry.load("/en_us.json", englishSources, logger);

    if (registryGerman == null || registryEnglish == null)
      Bukkit.getPluginManager().disablePlugin(this);
  }
}
