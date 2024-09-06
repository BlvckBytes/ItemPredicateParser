package me.blvckbytes.item_predicate_parser;

import me.blvckbytes.item_predicate_parser.translation.ILanguageRegistry;
import me.blvckbytes.item_predicate_parser.translation.LanguageRegistry;
import me.blvckbytes.item_predicate_parser.translation.TranslationLanguage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

public class ItemPredicateParserPlugin extends JavaPlugin {

  private static ItemPredicateParserPlugin instance;
  private LanguageRegistry languageRegistry;

  @Override
  public void onEnable() {
    try {
      this.languageRegistry = new LanguageRegistry(this);

      for (TranslationLanguage language : TranslationLanguage.values())
        this.languageRegistry.initializeRegistry(language);

      instance = this;
    } catch (Exception e) {
      getLogger().log(Level.SEVERE, "Could not download and or initialize languages", e);
      Bukkit.getPluginManager().disablePlugin(this);
    }
  }

  public ILanguageRegistry getLanguageRegistry() {
    return languageRegistry;
  }

  public static @Nullable ItemPredicateParserPlugin getInstance() {
    return instance;
  }
}
