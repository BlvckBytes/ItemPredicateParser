package me.blvckbytes.storage_query;

import me.blvckbytes.storage_query.translation.TranslationRegistry;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class StorageQueryPlugin extends JavaPlugin {

  @Override
  public void onEnable() {
    var logger = getLogger();

    var registryGerman = TranslationRegistry.load("/de_de.json", logger);
    var registryEnglish = TranslationRegistry.load("/en_us.json", logger);

    if (registryGerman == null || registryEnglish == null) {
      Bukkit.getPluginManager().disablePlugin(this);
      return;
    }

    var commandExecutor = new StorageQueryCommand(registryGerman, registryEnglish);
    var pluginCommand = Objects.requireNonNull(getCommand("lagersuche"));

    pluginCommand.setExecutor(commandExecutor);
    pluginCommand.setTabCompleter(commandExecutor);
  }
}
