package me.blvckbytes.item_predicate_parser.translation;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.blvckbytes.item_predicate_parser.parse.SubstringIndices;
import me.blvckbytes.item_predicate_parser.token.UnquotedStringToken;
import org.bukkit.Translatable;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Logger;

public class TranslationRegistry {

  private final JsonObject languageFile;
  private final Logger logger;
  private TranslatedTranslatable[] entries;

  public TranslationRegistry(JsonObject languageFile, Logger logger) {
    this.languageFile = languageFile;
    this.logger = logger;
  }

  public void initialize(Iterable<TranslatableSource> sources) throws IllegalStateException {
    var unsortedEntries = new ArrayList<TranslatedTranslatable>();

    for (var source : sources)
      createEntries(source, unsortedEntries);

    this.entries = unsortedEntries
      .stream()
      .sorted(Comparator.comparing(it -> it.normalizedTranslation))
      .toArray(TranslatedTranslatable[]::new);

    for (var entryIndex = 0; entryIndex < this.entries.length; ++entryIndex)
      this.entries[entryIndex].alphabeticalIndex = entryIndex;
  }

  public @Nullable TranslatedTranslatable lookup(Translatable translatable) {
    for (var entry : entries) {
      if (entry.translatable == translatable)
        return entry;
    }
    return null;
  }

  public SearchResult search(UnquotedStringToken query) {
    if (entries == null) {
      logger.warning("Tried to make use of search before initializing the registry");
      return new SearchResult(List.of(), false);
    }

    var result = new ArrayList<TranslatedTranslatable>();
    var queryParts = SubstringIndices.forString(query, query.value(), SubstringIndices.SEARCH_PATTERN_DELIMITER);

    var isWildcardPresent = false;

    for (var entry : entries) {
      var pendingQueryParts = new ArrayList<>(queryParts);
      var pendingTextParts = new ArrayList<>(entry.partIndices);

      isWildcardPresent |= SubstringIndices.matchQuerySubstrings(
        query.value(), pendingQueryParts,
        entry.normalizedTranslation, pendingTextParts
      );

      if (!pendingQueryParts.isEmpty())
        continue;

      // If there's a wildcard, disregard full matches
      if (isWildcardPresent && pendingTextParts.isEmpty())
        continue;

      result.add(entry);
    }

    return new SearchResult(result, isWildcardPresent);
  }

  private void createEntries(TranslatableSource source, ArrayList<TranslatedTranslatable> output) throws IllegalStateException {
    for (var translatable : source.items()) {
      var translationKey = translatable.getTranslationKey();
      var translationValue = getTranslationOrNull(languageFile, translationKey);

      if (translationValue == null)
        throw new IllegalStateException("Could not locate translation-value for key " + translationKey);

      var normalizedTranslationValue = TranslatedTranslatable.normalize(translationValue);

      var entry = new TranslatedTranslatable(source, translatable, normalizedTranslationValue);
      boolean hadCollision = false;

      for (var outputIndex = 0; outputIndex < output.size(); ++outputIndex) {
        var existingEntry = output.get(outputIndex);

        if (!existingEntry.normalizedTranslation.equalsIgnoreCase(entry.normalizedTranslation))
          continue;

        if (existingEntry.source == entry.source) {
          // TODO: Add auto-incrementing collision-prefixes on colliding members within the same source
          continue;
        }

        output.set(outputIndex, new TranslatedTranslatable(
          existingEntry.source,
          existingEntry.translatable,
          existingEntry.source.collisionPrefix() + existingEntry.normalizedTranslation
        ));

        output.add(new TranslatedTranslatable(
          source,
          translatable,
          source.collisionPrefix() + normalizedTranslationValue
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
}