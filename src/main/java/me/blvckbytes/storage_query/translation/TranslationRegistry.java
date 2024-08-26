package me.blvckbytes.storage_query.translation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.blvckbytes.storage_query.parse.SearchWildcardPresence;
import me.blvckbytes.storage_query.parse.SubstringIndices;
import org.jetbrains.annotations.Nullable;

import java.io.InputStreamReader;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TranslationRegistry {

  private static final Gson GSON = new GsonBuilder().create();

  private final JsonObject languageFile;
  private final Logger logger;
  private TranslatedTranslatable[] entries;

  public TranslationRegistry(JsonObject languageFile, Logger logger) {
    this.languageFile = languageFile;
    this.logger = logger;
  }

  public void initialize(Iterable<TranslatableSource> sources) {
    var unsortedEntries = new ArrayList<TranslatedTranslatable>();

    for (var source : sources)
      createEntries(source, unsortedEntries);

    this.entries = unsortedEntries
      .stream()
      .sorted(Comparator.comparing(TranslatedTranslatable::translation))
      .toArray(TranslatedTranslatable[]::new);
  }

  public SearchResult search(String text) {
    if (entries == null) {
      logger.warning("Tried to make use of search before initializing the registry");
      return new SearchResult();
    }

    var result = new ArrayList<TranslatedTranslatable>();
    var textParts = SubstringIndices.forString(text, SubstringIndices.SEARCH_PATTERN_DELIMITERS);

    var wildcardPresence = SearchWildcardPresence.ABSENT;

    for (var entry : entries) {
      var pendingTextParts = new ArrayList<>(textParts);

      wildcardPresence = SubstringIndices.matchQuerySubstrings(
        text, pendingTextParts,
        entry.translation(), new ArrayList<>(entry.partIndices())
      );

      if (wildcardPresence == SearchWildcardPresence.CONFLICT_OCCURRED_REPEATEDLY)
        return new SearchResult(List.of(), wildcardPresence);

      if (pendingTextParts.isEmpty())
        result.add(entry);
    }

    return new SearchResult(result, wildcardPresence);
  }

  private void createEntries(TranslatableSource source, ArrayList<TranslatedTranslatable> output) {
    for (var translatable : source.items()) {
      var translationKey = translatable.getTranslationKey();
      var translationValue = getTranslationOrNull(languageFile, translationKey);

      if (translationValue == null) {
        logger.warning("Could not locate translation-value for key " + translationKey);
        continue;
      }

      var entry = new TranslatedTranslatable(source, translatable, translationValue);
      boolean hadCollision = false;

      for (var outputIndex = 0; outputIndex < output.size(); ++outputIndex) {
        var existingEntry = output.get(outputIndex);

        if (!(
          existingEntry.normalizedName().equalsIgnoreCase(entry.normalizedName()) &&
          existingEntry.source() != entry.source() // Do not prefix within the same source - useless
        ))
          continue;

        output.set(outputIndex, new TranslatedTranslatable(
          existingEntry.source(),
          existingEntry.translatable(),
          existingEntry.source().collisionPrefix() + existingEntry.translation()
        ));

        output.add(new TranslatedTranslatable(
          source,
          translatable,
          source.collisionPrefix() + translationValue
        ));

        hadCollision = true;
        break;
      }

      if (!hadCollision)
        output.add(entry);
    }
  }

  private @Nullable String getTranslationOrNull(JsonObject languageFile, String translationKey) {
    var translationValue = languageFile.get(translationKey);

    if (translationValue == null)
      return null;

    if (!(translationValue instanceof JsonPrimitive))
      return null;

    return translationValue.getAsString();
  }

  public static @Nullable TranslationRegistry load(
    String absoluteLanguageFilePath,
    Iterable<TranslatableSource> translatableSources,
    Logger logger
  ) {
    try (var inputStream = TranslationRegistry.class.getResourceAsStream(absoluteLanguageFilePath)) {
      if (inputStream == null)
        throw new IllegalStateException("Resource stream was null");

      var languageJson = GSON.fromJson(new InputStreamReader(inputStream), JsonObject.class);

      var registry = new TranslationRegistry(languageJson, logger);

      registry.initialize(translatableSources);

      logger.info("Loaded registry for translation-file " + absoluteLanguageFilePath);
      return registry;
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Could not load translation-file " + absoluteLanguageFilePath, e);
      return null;
    }
  }
}