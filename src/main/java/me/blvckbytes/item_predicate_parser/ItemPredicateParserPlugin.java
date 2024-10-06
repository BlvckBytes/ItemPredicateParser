package me.blvckbytes.item_predicate_parser;

import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import me.blvckbytes.bukkitevaluable.ConfigManager;
import me.blvckbytes.item_predicate_parser.config.MainSection;
import me.blvckbytes.item_predicate_parser.translation.LanguageRegistry;
import me.blvckbytes.item_predicate_parser.translation.resolver.PluginTranslationResolver;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.logging.Level;

public class ItemPredicateParserPlugin extends JavaPlugin {

  private static ItemPredicateParserPlugin instance;
  private PredicateHelper predicateHelper;

  @Override
  public void onEnable() {
    var logger = getLogger();

    try {
      var configManager = new ConfigManager(this);
      var config = new ConfigKeeper<>(configManager, "config.yml", MainSection.class);

      var languageRegistry = new LanguageRegistry(this, new PluginTranslationResolver(this));
      this.predicateHelper = new PredicateHelper(languageRegistry, config);

      Objects.requireNonNull(getCommand(ItemPredicateParserCommand.COMMAND_NAME)).setExecutor(
        new ItemPredicateParserCommand(predicateHelper, config, logger)
      );

      Bukkit.getServer().getPluginManager().registerEvents(new CommandSendListener(this), this);

      instance = this;
    } catch (Throwable e) {
      getLogger().log(Level.SEVERE, "Could not download and or initialize languages", e);
      Bukkit.getPluginManager().disablePlugin(this);
    }
  }

  public PredicateHelper getPredicateHelper() {
    return predicateHelper;
  }

  public static @Nullable ItemPredicateParserPlugin getInstance() {
    return instance;
  }
}
