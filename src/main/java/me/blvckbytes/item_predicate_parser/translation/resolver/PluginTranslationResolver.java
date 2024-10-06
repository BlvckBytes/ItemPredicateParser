package me.blvckbytes.item_predicate_parser.translation.resolver;

import me.blvckbytes.item_predicate_parser.translation.keyed.LangKeyed;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class PluginTranslationResolver extends TranslationResolver {

  private static final String ECO_ENCHANTS_RESOLVER_PATH = "me/blvckbytes/item_predicate_parser/translation/resolver/EcoEnchantsResolver";
  private static final String EXCELLENT_ENCHANTS_RESOLVER_PATH = "me/blvckbytes/item_predicate_parser/translation/resolver/ExcellentEnchantsResolver";

  private final List<TranslationResolver> resolvers;

  public PluginTranslationResolver(Plugin plugin) throws Exception {
    super(plugin);

    this.resolvers = new ArrayList<>();

    Plugin loadedPlugin;

    if ((loadedPlugin = Bukkit.getPluginManager().getPlugin("EcoEnchants")) != null) {
      resolvers.add(loadResolver(ECO_ENCHANTS_RESOLVER_PATH, loadedPlugin));
      plugin.getLogger().log(Level.INFO, "Loaded resolver for EcoEnchants");
    }

    if ((loadedPlugin = Bukkit.getPluginManager().getPlugin("ExcellentEnchants")) != null) {
      resolvers.add(loadResolver(EXCELLENT_ENCHANTS_RESOLVER_PATH, loadedPlugin));
      plugin.getLogger().log(Level.INFO, "Loaded resolver for ExcellentEnchants");
    }
  }

  @Override
  public @Nullable String resolve(LangKeyed<?> langKeyed) {
    String result;

    for (var resolver : resolvers) {
      if ((result = resolver.resolve(langKeyed)) != null)
        return result;
    }

    return null;
  }

  private static TranslationResolver loadResolver(String path, Plugin loadedPlugin) throws Exception {
    return (TranslationResolver) Class
      .forName(path.replace('/', '.'))
      .getConstructor(Plugin.class)
      .newInstance(loadedPlugin);
  }
}
