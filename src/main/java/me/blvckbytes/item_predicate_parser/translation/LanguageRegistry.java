package me.blvckbytes.item_predicate_parser.translation;

import at.blvckbytes.cm_mapper.ConfigKeeper;
import at.blvckbytes.cm_mapper.ReloadPriority;
import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import me.blvckbytes.item_predicate_parser.TranslationLanguageRegistry;
import me.blvckbytes.item_predicate_parser.config.MainSection;
import me.blvckbytes.item_predicate_parser.translation.keyed.*;
import me.blvckbytes.item_predicate_parser.translation.resolver.TranslationResolver;
import me.blvckbytes.item_predicate_parser.translation.version.IVersionDependentCode;
import me.blvckbytes.item_predicate_parser.translation.version.VersionDependentCodeFactory;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

public class LanguageRegistry implements TranslationLanguageRegistry {

  private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

  private final ConfigKeeper<MainSection> config;
  private final @Nullable TranslationResolver translationResolver;
  private final AssetIndex assetIndex;
  private final File languagesFolder;
  private final Logger logger;
  private final IVersionDependentCode versionDependentCode;

  private final Map<TranslationLanguage, TranslationRegistry> translationRegistryByLanguage;

  public LanguageRegistry(
    Plugin plugin,
    ConfigKeeper<MainSection> config,
    @Nullable TranslationResolver translationResolver
  ) throws Throwable {
    this.config = config;
    this.translationResolver = translationResolver;
    this.translationRegistryByLanguage = new HashMap<>();
    this.logger = plugin.getLogger();
    this.assetIndex = new AssetIndex(null, plugin.getLogger());
    this.languagesFolder = Paths.get(plugin.getDataFolder().getAbsolutePath(), "languages", assetIndex.serverVersion.original()).toFile();

    if (!this.languagesFolder.isDirectory()) {
      if (!this.languagesFolder.mkdirs())
        throw new IllegalStateException("Could not create directory " + this.languagesFolder);

      logger.info("Created folder to house language files");
    }

    this.versionDependentCode = new VersionDependentCodeFactory(assetIndex.serverVersion, logger).get();

    for (TranslationLanguage language : TranslationLanguage.values())
      initializeTranslationRegistry(language);

    config.registerReloadListener(() -> {
      // Update variables by re-making sources
      for (var registry : translationRegistryByLanguage.values())
        registry.initialize(makeSources(registry.language.collisionPrefixes, registry.languageFile));

      Bukkit.getPluginManager().callEvent(new PredicateSourcesReloadEvent());
    }, ReloadPriority.HIGH);
  }

  private JsonObject accessOrDownloadLanguageFile(TranslationLanguage language, boolean overwrite) throws Exception {
    var localFile = new File(this.languagesFolder, language.assetFileNameWithoutExtension + ".json");

    if (overwrite && localFile.exists()) {
      if (!localFile.delete())
        throw new IllegalStateException("Could not delete existing language file " + localFile);
    }

    if (localFile.exists()) {
      try (
        var scanner = new Scanner(localFile, Charsets.UTF_8)
      ) {
        return gson.fromJson(scanner.useDelimiter("\\A").next(), JsonObject.class);
      }
    }

    logger.info("Downloading language-file " + language.assetFileNameWithoutExtension);

    var languageObject = assetIndex.getLanguageFile(language);

    try (
      var outputStream = new FileOutputStream(localFile);
      var streamWriter = new OutputStreamWriter(outputStream, Charsets.UTF_8)
    ) {
      streamWriter.write(gson.toJson(languageObject));
    }

    return languageObject;
  }

  @Override
  public @NotNull TranslationRegistry getTranslationRegistry(TranslationLanguage language) {
    return translationRegistryByLanguage.get(language);
  }

  private void initializeTranslationRegistry(TranslationLanguage language) throws Exception {
    JsonObject languageFile;

    try {
      languageFile = accessOrDownloadLanguageFile(language, false);
    } catch (JsonSyntaxException e1) {
      try {
        languageFile = accessOrDownloadLanguageFile(language, true);
      } catch (JsonSyntaxException e2) {
        throw new IllegalStateException("Could not successfully parse language-file " + language.assetFileNameWithoutExtension);
      }
    }

    TranslationRegistry registry = new TranslationRegistry(language, languageFile, versionDependentCode, translationResolver, logger);
    registry.initialize(makeSources(language.collisionPrefixes, languageFile));
    translationRegistryByLanguage.put(language, registry);
  }

  private List<LangKeyedSource> makeSources(CollisionPrefixes collisionPrefixes, JsonObject languageJson) {
    var result = new ArrayList<LangKeyedSource>();

    result.add(new LangKeyedSource(
      versionDependentCode.getEnchantments(),
      collisionPrefixes.forEnchantments())
    );

    result.add(new LangKeyedSource(
      versionDependentCode.getEffects(),
      collisionPrefixes.forEffects())
    );

    result.add(new LangKeyedSource(
      versionDependentCode.getPotionTypes(),
      collisionPrefixes.forPotionTypes())
    );

    result.add(new LangKeyedSource(
      versionDependentCode.getItemMaterials(languageJson),
      collisionPrefixes.forMaterials()
    ));

    var instruments = versionDependentCode.getInstruments();

    if (instruments != null)
      result.add(new LangKeyedSource(instruments, collisionPrefixes.forInstruments()));

    result.add(new LangKeyedSource(List.of(
      DeteriorationKey.INSTANCE,
      NegationKey.INSTANCE,
      DisjunctionKey.INSTANCE,
      ConjunctionKey.INSTANCE,
      ExactKey.INSTANCE,
      AmountKey.INSTANCE,
      InnerAllKey.INSTANCE,
      InnerAllOrSelfKey.INSTANCE,
      InnerSomeKey.INSTANCE,
      InnerSomeOrSelfKey.INSTANCE,
      AnyKey.INSTANCE,
      HasNameKey.INSTANCE
    ), ""));

    result.add(new LangKeyedSource(config.rootSection.variables._variableKeys, ""));

    return result;
  }
}
