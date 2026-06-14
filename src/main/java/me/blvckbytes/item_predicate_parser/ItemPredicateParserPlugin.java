package me.blvckbytes.item_predicate_parser;

import at.blvckbytes.cm_mapper.ConfigHandler;
import at.blvckbytes.cm_mapper.ConfigKeeper;
import at.blvckbytes.cm_mapper.ConfigKeeperReloadEvent;
import at.blvckbytes.cm_mapper.section.command.CommandUpdater;
import me.blvckbytes.item_predicate_parser.command.CommandSendListener;
import me.blvckbytes.item_predicate_parser.command.ItemPredicateParserCommand;
import me.blvckbytes.item_predicate_parser.config.ItemPredicateParserCommandSection;
import me.blvckbytes.item_predicate_parser.config.MainSection;
import me.blvckbytes.item_predicate_parser.display.overview.VariablesDisplayHandler;
import me.blvckbytes.item_predicate_parser.translation.LanguageRegistry;
import me.blvckbytes.item_predicate_parser.translation.resolver.PluginTranslationResolver;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Objects;
import java.util.logging.Level;

public class ItemPredicateParserPlugin extends JavaPlugin implements Listener {

  private static ItemPredicateParserPlugin instance;

  private @Nullable PredicateHelper predicateHelper;
  private @Nullable LanguageRegistry languageRegistry;
  private @Nullable VariablesDisplayHandler variablesDisplayHandler;
  private @Nullable NameScopedKeyValueStore keyValueStore;
  private @Nullable WebApiServer webApiServer;
  private @Nullable PluginCommand mainCommand;
  private @Nullable ConfigKeeper<MainSection> config;
  private @Nullable Runnable updateCommands;

  private int time;

  @Override
  public void onEnable() {
    var logger = getLogger();

    try {
      var configHandler = new ConfigHandler(this, "config");

      configHandler.saveDefaultConfig("template_de_de.yml", true);

      config = new ConfigKeeper<>(configHandler, "config.yml", MainSection.class);

      keyValueStore = new NameScopedKeyValueStore(getFileAndEnsureExistence("user-preferences.json"), logger);

      Bukkit.getScheduler().runTaskTimerAsynchronously(this, keyValueStore::saveToDisk, 20 * 60L, 20 * 60L);

      languageRegistry = new LanguageRegistry(this, config, new PluginTranslationResolver(this));
      this.predicateHelper = new PredicateHelper(keyValueStore, languageRegistry, config);
      Bukkit.getPluginManager().registerEvents(predicateHelper, this);

      variablesDisplayHandler = new VariablesDisplayHandler(config, this);
      Bukkit.getServer().getPluginManager().registerEvents(variablesDisplayHandler, this);

      var commandUpdater = new CommandUpdater(this);
      mainCommand = Objects.requireNonNull(getCommand(ItemPredicateParserCommandSection.INITIAL_NAME));

      var commandHandler = new ItemPredicateParserCommand(variablesDisplayHandler, languageRegistry, keyValueStore, predicateHelper, config, logger);

      mainCommand.setExecutor(commandHandler);
      Bukkit.getServer().getPluginManager().registerEvents(commandHandler, this);

      Bukkit.getScheduler().runTaskTimer(this, () -> {
        ++time;
        commandHandler.tick(time);
      }, 0, 1);

      updateCommands = () -> {
        config.rootSection.commands.itemPredicateParser.apply(mainCommand, commandUpdater);
        commandUpdater.trySyncCommands();
      };

      updateCommands.run();

      Bukkit.getServer().getPluginManager().registerEvents(new CommandSendListener(this, config), this);

      webApiServer = new WebApiServer(languageRegistry, config, logger);
      Bukkit.getPluginManager().registerEvents(webApiServer, this);

      webApiServer.restart();

      Bukkit.getPluginManager().registerEvents(this, this);

      instance = this;
    } catch (Throwable e) {
      getLogger().log(Level.SEVERE, "Could not download and or initialize languages", e);
      Bukkit.getPluginManager().disablePlugin(this);
    }
  }

  @Override
  public void onDisable() {
    if (variablesDisplayHandler != null) {
      variablesDisplayHandler.onShutdown();
      variablesDisplayHandler = null;
    }

    if (keyValueStore != null) {
      keyValueStore.saveToDisk();
      keyValueStore = null;
    }

    if (webApiServer != null) {
      webApiServer.stop();
      webApiServer = null;
    }
  }

  @EventHandler
  public void onConfigReload(ConfigKeeperReloadEvent event) {
    if (event.configKeeper == config && updateCommands != null)
      updateCommands.run();
  }

  public TranslationLanguageRegistry getTranslationLanguageRegistry() {
    return languageRegistry;
  }

  public PredicateHelper getPredicateHelper() {
    return predicateHelper;
  }

  public PluginCommand getMainCommand() {
    return mainCommand;
  }

  public static @Nullable ItemPredicateParserPlugin getInstance() {
    return instance;
  }

  private File getFileAndEnsureExistence(String name) throws Exception {
    var file = new File(getDataFolder(), name);

    if (!file.exists()) {
      var parentDirectory = file.getParentFile();

      if (!parentDirectory.exists() && !parentDirectory.mkdirs())
        throw new IllegalStateException("Could not create parent-directories of the file " + file);

      if (!file.createNewFile())
        throw new IllegalStateException("Could not create the file " + file);
    }

    return file;
  }
}
