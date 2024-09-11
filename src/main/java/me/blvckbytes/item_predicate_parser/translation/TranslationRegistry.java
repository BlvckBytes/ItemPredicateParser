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
      .sorted(Comparator.comparing(it -> it.normalizedPrefixedTranslation))
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
      var pendingTextParts = entry.getPartIndicesCopy();

      isWildcardPresent |= SubstringIndices.matchQuerySubstrings(
        query.value(), pendingQueryParts,
        entry.normalizedPrefixedTranslation, pendingTextParts
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
    var buckets = new HashMap<String, ArrayList<Translatable>>();

    for (var translatable : source.items()) {
      var translationValue = getTranslationOrNull(translatable);

      if (translationValue == null)
        throw new IllegalStateException("Could not locate translation-value for key " + translatable.getTranslationKey());

      var normalizedTranslationValue = TranslatedTranslatable.normalize(translationValue);

      var bucket = buckets.computeIfAbsent(normalizedTranslationValue, k -> new ArrayList<>());
      bucket.add(translatable);
    }

    for (var bucketEntry : buckets.entrySet()) {
      var bucketNormalizedUnPrefixedTranslation = bucketEntry.getKey();
      var bucketItems = bucketEntry.getValue();
      var bucketSize = bucketItems.size();

      // Prefix all items of other sources that would collide with the item about to be added
      for (var itemIndex = 0; itemIndex < bucketSize; ++itemIndex) {
        var bucketItem = bucketItems.get(itemIndex);

        boolean hadCollision = false;

        for (var outputIndex = 0; outputIndex < output.size(); ++outputIndex) {
          var existingEntry = output.get(outputIndex);

          // Do not add cross-source collision prefixes on same-source items, as the incrementing
          // bucket index already takes care of these kinds of collision
          if (existingEntry.source == source)
            continue;

          if (!existingEntry.normalizedUnPrefixedTranslation.equalsIgnoreCase(bucketNormalizedUnPrefixedTranslation))
            continue;

          output.set(outputIndex, new TranslatedTranslatable(
            existingEntry.source,
            existingEntry.translatable,
            existingEntry.normalizedUnPrefixedTranslation,
            existingEntry.source.collisionPrefix() + existingEntry.normalizedPrefixedTranslation
          ));

          hadCollision = true;
        }

        var newItemPrefixedTranslation = bucketNormalizedUnPrefixedTranslation;

        // Incrementing same-source prefixes should be nearest to the translation
        if (bucketSize > 1)
          newItemPrefixedTranslation = (itemIndex + 1) + "-" + newItemPrefixedTranslation;

        if (hadCollision)
          newItemPrefixedTranslation = source.collisionPrefix() + newItemPrefixedTranslation;

        output.add(new TranslatedTranslatable(source, bucketItem, bucketNormalizedUnPrefixedTranslation, newItemPrefixedTranslation));
      }
    }
  }

  public @Nullable String getTranslationOrNull(Translatable translatable) {
    var translationValue = languageFile.get(translatable.getTranslationKey());

    if (translationValue == null)
      return null;

    if (!(translationValue instanceof JsonPrimitive))
      return null;

    return translationValue.getAsString();
  }
}