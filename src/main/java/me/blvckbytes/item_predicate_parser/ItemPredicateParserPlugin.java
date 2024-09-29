package me.blvckbytes.item_predicate_parser;

import me.blvckbytes.bukkitevaluable.ConfigManager;
import me.blvckbytes.item_predicate_parser.config.MainSection;
import me.blvckbytes.item_predicate_parser.translation.LanguageRegistry;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

public class ItemPredicateParserPlugin extends JavaPlugin {

  private static ItemPredicateParserPlugin instance;
  private PredicateHelper predicateHelper;

  @Override
  public void onEnable() {
    try {
      var configManager = new ConfigManager(this);
      var configMapper = configManager.loadConfig("config.yml");
      var mainSection = configMapper.mapSection(null, MainSection.class);

      var languageRegistry = new LanguageRegistry(this);
      this.predicateHelper = new PredicateHelper(languageRegistry, mainSection);

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
