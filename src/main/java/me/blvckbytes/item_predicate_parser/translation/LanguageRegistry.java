package me.blvckbytes.item_predicate_parser.translation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import me.blvckbytes.item_predicate_parser.translation.keyed.*;
import org.apache.commons.io.FileUtils;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

public class LanguageRegistry implements ILanguageRegistry {

  private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

  private final AssetIndex assetIndex;
  private final File languagesFolder;
  private final Logger logger;

  private final Map<TranslationLanguage, TranslationRegistry> registryByLanguage;

  public LanguageRegistry(Plugin plugin) throws Exception {
    this.registryByLanguage = new HashMap<>();
    this.logger = plugin.getLogger();
    this.assetIndex = new AssetIndex(null);
    this.languagesFolder = Paths.get(plugin.getDataFolder().getAbsolutePath(), "languages", assetIndex.serverVersion).toFile();

    if (!this.languagesFolder.isDirectory()) {
      if (!this.languagesFolder.mkdirs())
        throw new IllegalStateException("Could not create directory " + this.languagesFolder);

      logger.info("Created folder to house language files");
    }
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

  @Override
  public @Nullable TranslationRegistry getTranslationRegistry(TranslationLanguage language) {
    return registryByLanguage.get(language);
  }

  public void initializeRegistry(TranslationLanguage language) throws Exception {
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

    TranslationRegistry registry = new TranslationRegistry(languageFile, logger);
    registry.initialize(makeSources(language.collisionPrefixes));
    registryByLanguage.put(language, registry);
  }

  private List<LangKeyedSource> makeSources(CollisionPrefixes collisionPrefixes) {
    var result = new ArrayList<LangKeyedSource>();

    result.add(new LangKeyedSource(
      Registry.ENCHANTMENT.stream().map(LangKeyedEnchantment::new).toList(),
      collisionPrefixes.forEnchantments())
    );

    result.add(new LangKeyedSource(
      Registry.EFFECT.stream().map(LangKeyedPotionEffectType::new).toList(),
      collisionPrefixes.forEffects())
    );

    result.add(new LangKeyedSource(
      Registry.MATERIAL.stream().filter(Material::isItem).map(LangKeyedItemMaterial::new).toList(),
      collisionPrefixes.forMaterials()
    ));

    result.add(new LangKeyedSource(
      Registry.INSTRUMENT.stream().map(LangKeyedMusicInstrument::new).toList(),
      collisionPrefixes.forMaterials()
    ));

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
