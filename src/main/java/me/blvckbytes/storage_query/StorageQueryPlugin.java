package me.blvckbytes.storage_query;

import me.blvckbytes.storage_query.translation.DeteriorationKey;
import me.blvckbytes.storage_query.translation.TranslatableSource;
import me.blvckbytes.storage_query.translation.TranslationRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Registry;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class StorageQueryPlugin extends JavaPlugin {

  @Override
  public void onEnable() {
    var logger = getLogger();

    var germanSources = Arrays.asList(
      new TranslatableSource(Registry.ENCHANTMENT, "(Verzauberung) "),
      new TranslatableSource(Registry.EFFECT, "(Effekt) "),
      new TranslatableSource(Registry.MATERIAL, "(Material) "),
      new TranslatableSource(List.of(DeteriorationKey.INSTANCE), "")
    );

    var englishSources = Arrays.asList(
      new TranslatableSource(Registry.ENCHANTMENT, "(Enchantment) "),
      new TranslatableSource(Registry.EFFECT, "(Effect) "),
      new TranslatableSource(Registry.MATERIAL, "(Material) "),
      new TranslatableSource(List.of(DeteriorationKey.INSTANCE), "")
    );

    var registryGerman = TranslationRegistry.load("/de_de.json", germanSources, logger);
    var registryEnglish = TranslationRegistry.load("/en_us.json", englishSources, logger);

    if (registryGerman == null || registryEnglish == null) {
      Bukkit.getPluginManager().disablePlugin(this);
      return;
    }

    var resultDisplay = new ResultDisplayHandler(this);

    getServer().getPluginManager().registerEvents(resultDisplay, this);

    var commandExecutor = new StorageQueryCommand(registryGerman, registryEnglish, resultDisplay);
    var pluginCommand = Objects.requireNonNull(getCommand("lagersuche"));

    pluginCommand.setExecutor(commandExecutor);
    pluginCommand.setTabCompleter(commandExecutor);
  }
}
