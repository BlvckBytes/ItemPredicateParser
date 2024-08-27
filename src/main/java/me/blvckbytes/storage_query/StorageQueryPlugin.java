package me.blvckbytes.storage_query;

import me.blvckbytes.storage_query.translation.DeteriorationKey;
import me.blvckbytes.storage_query.translation.NegationKey;
import me.blvckbytes.storage_query.translation.TranslatableSource;
import me.blvckbytes.storage_query.translation.TranslationRegistry;
import me.blvckbytes.storage_query.ui.ResultDisplayHandler;
import me.blvckbytes.storage_query.ui.StorageQueryCommand;
import org.bukkit.Bukkit;
import org.bukkit.Registry;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class StorageQueryPlugin extends JavaPlugin {

  private ResultDisplayHandler displayHandler = null;

  @Override
  public void onEnable() {
    var logger = getLogger();

    var germanSources = Arrays.asList(
      new TranslatableSource(Registry.ENCHANTMENT, "[Verzauberung] "),
      new TranslatableSource(Registry.EFFECT, "[Effekt] "),
      new TranslatableSource(Registry.MATERIAL, "[Typ] "),
      new TranslatableSource(List.of(DeteriorationKey.INSTANCE), ""),
      new TranslatableSource(List.of(NegationKey.INSTANCE), "")
    );

    var englishSources = Arrays.asList(
      new TranslatableSource(Registry.ENCHANTMENT, "[Enchantment] "),
      new TranslatableSource(Registry.EFFECT, "[Effect] "),
      new TranslatableSource(Registry.MATERIAL, "[Material] "),
      new TranslatableSource(List.of(DeteriorationKey.INSTANCE), ""),
      new TranslatableSource(List.of(NegationKey.INSTANCE), "")
    );

    var registryGerman = TranslationRegistry.load("/de_de.json", germanSources, logger);
    var registryEnglish = TranslationRegistry.load("/en_us.json", englishSources, logger);

    if (registryGerman == null || registryEnglish == null) {
      Bukkit.getPluginManager().disablePlugin(this);
      return;
    }

    displayHandler = new ResultDisplayHandler(this);

    getServer().getPluginManager().registerEvents(displayHandler, this);

    var commandExecutor = new StorageQueryCommand(registryGerman, registryEnglish, displayHandler);
    var pluginCommand = Objects.requireNonNull(getCommand("lagersuche"));

    pluginCommand.setExecutor(commandExecutor);
    pluginCommand.setTabCompleter(commandExecutor);
  }

  @Override
  public void onDisable() {
    if (this.displayHandler != null)
      displayHandler.onShutdown();
  }
}
