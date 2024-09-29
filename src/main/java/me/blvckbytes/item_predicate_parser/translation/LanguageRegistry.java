package me.blvckbytes.item_predicate_parser.translation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import me.blvckbytes.item_predicate_parser.translation.keyed.*;
import me.blvckbytes.item_predicate_parser.translation.version.IVersionDependentCode;
import org.apache.commons.io.FileUtils;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

public class LanguageRegistry {

  private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

  private final AssetIndex assetIndex;
  private final File languagesFolder;
  private final Logger logger;
  private final IVersionDependentCode versionDependentCode;

  private final Map<TranslationLanguage, TranslationRegistry> translationRegistryByLanguage;

  public LanguageRegistry(Plugin plugin) throws Throwable {
    this.translationRegistryByLanguage = new HashMap<>();
    this.logger = plugin.getLogger();
    this.assetIndex = new AssetIndex(null);
    this.languagesFolder = Paths.get(plugin.getDataFolder().getAbsolutePath(), "languages", assetIndex.serverVersion.original()).toFile();

    if (!this.languagesFolder.isDirectory()) {
      if (!this.languagesFolder.mkdirs())
        throw new IllegalStateException("Could not create directory " + this.languagesFolder);

      logger.info("Created folder to house language files");
    }

    this.versionDependentCode = new VersionDependentCodeFactory(assetIndex.serverVersion, logger).get();

    for (TranslationLanguage language : TranslationLanguage.values())
      initializeTranslationRegistry(language);
  }

  private JsonObject accessOrDownloadLanguageFile(TranslationLanguage language, boolean overwrite) throws Exception {
    var localFile = new File(this.languagesFolder, language.assetFileNameWithoutExtension + ".json");

    if (overwrite && localFile.exists()) {
      if (!localFile.delete())
        throw new IllegalStateException("Could not delete existing language file " + localFile);
    }

    if (localFile.exists())
      return gson.fromJson(FileUtils.readFileToString(localFile, StandardCharsets.UTF_8), JsonObject.class);

    logger.info("Downloading language-file " + language.assetFileNameWithoutExtension);

    var languageObject = assetIndex.getLanguageFile(language);
    FileUtils.writeStringToFile(localFile, gson.toJson(languageObject), StandardCharsets.UTF_8);
    return languageObject;
  }

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

    language.customTranslations.apply(languageFile);

    TranslationRegistry registry = new TranslationRegistry(languageFile, versionDependentCode, logger);
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
      AmountKey.INSTANCE
    ), ""));

    return result;
  }
}
