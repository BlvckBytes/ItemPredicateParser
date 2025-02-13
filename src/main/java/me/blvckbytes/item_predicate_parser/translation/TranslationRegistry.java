package me.blvckbytes.item_predicate_parser.translation;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.blvckbytes.item_predicate_parser.SingletonTranslationRegistry;
import me.blvckbytes.item_predicate_parser.parse.ItemPredicateParseException;
import me.blvckbytes.item_predicate_parser.parse.ParseConflict;
import me.blvckbytes.item_predicate_parser.token.UnquotedStringToken;
import me.blvckbytes.item_predicate_parser.translation.keyed.LangKeyed;
import me.blvckbytes.item_predicate_parser.translation.resolver.TranslationResolver;
import me.blvckbytes.item_predicate_parser.translation.version.IVersionDependentCode;
import me.blvckbytes.syllables_matcher.Syllables;
import me.blvckbytes.syllables_matcher.SyllablesMatcher;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Logger;

public class TranslationRegistry implements SingletonTranslationRegistry {

  private final TranslationLanguage language;
  private final JsonObject languageFile;
  private final IVersionDependentCode versionDependentCode;
  private final @Nullable TranslationResolver translationResolver;
  private final Logger logger;
  private TranslatedLangKeyed<?>[] entries;
  private Map<Object, String> translationByWrapped;

  public TranslationRegistry(
    TranslationLanguage language,
    JsonObject languageFile,
    IVersionDependentCode versionDependentCode,
    @Nullable TranslationResolver translationResolver,
    Logger logger
  ) {
    this.language = language;
    this.languageFile = languageFile;
    this.versionDependentCode = versionDependentCode;
    this.translationResolver = translationResolver;
    this.logger = logger;
  }

  public IVersionDependentCode getVersionDependentCode() {
    return versionDependentCode;
  }

  public void initialize(Iterable<LangKeyedSource> sources) throws IllegalStateException {
    var unsortedEntries = new ArrayList<TranslatedLangKeyed<?>>();
    translationByWrapped = new HashMap<>();

    for (var source : sources)
      createEntries(source, unsortedEntries, translationByWrapped);

    this.entries = unsortedEntries
      .stream()
      .sorted(Comparator.comparing(it -> it.normalizedPrefixedTranslation))
      .toArray(TranslatedLangKeyed[]::new);

    var entryIndex = 0;

    for (; entryIndex < this.entries.length; ++entryIndex)
      this.entries[entryIndex].alphabeticalIndex = entryIndex;

    logger.info("Loaded " + entryIndex + " entries for language " + language.assetFileNameWithoutExtension);
  }

  @Override
  public @Nullable String getTranslationBySingleton(Object instance) {
    return translationByWrapped.get(instance);
  }

  public @Nullable TranslatedLangKeyed<?> lookup(LangKeyed<?> langKeyed) {
    if (entries == null)
      return null;

    for (var entry : entries) {
      if (entry.langKeyed.equals(langKeyed))
        return entry;
    }

    return null;
  }

  public SearchResult search(UnquotedStringToken query) {
    if (entries == null) {
      logger.warning("Tried to make use of search before initializing the registry");
      return new SearchResult(List.of(), false);
    }

    var result = new ArrayList<TranslatedLangKeyed<?>>();
    var querySyllablesResult = Syllables.forStringWithWildcardSupport(query.value(), Syllables.DELIMITER_SEARCH_PATTERN);

    if (querySyllablesResult.numberOfWildcardSyllables() > 1)
      throw new ItemPredicateParseException(query, ParseConflict.MULTIPLE_SEARCH_PATTERN_WILDCARDS);

    if (querySyllablesResult.numberOfWildcardSyllables() != 0 && querySyllablesResult.numberOfNonWildcardSyllables() == 0)
      throw new ItemPredicateParseException(query, ParseConflict.ONLY_SEARCH_PATTERN_WILDCARD);

    var querySyllables = querySyllablesResult.syllables();
    var isWildcardMode = querySyllables.isWildcardMode();

    var matcher = new SyllablesMatcher();
    matcher.setQuery(querySyllables);

    for (var entryIndex = 0; entryIndex < entries.length; ++entryIndex) {
      var entry = entries[entryIndex];

      if (entryIndex != 0)
        matcher.resetQueryMatches();

      matcher.setTarget(entry.syllables);

      matcher.match();

      if (matcher.hasUnmatchedQuerySyllables())
        continue;

      // If there's a wildcard, disregard full matches; that's just a design-decision
      if (isWildcardMode && !matcher.hasUnmatchedTargetSyllables())
        continue;

      result.add(entry);
    }

    return new SearchResult(result, isWildcardMode);
  }

  private void createEntries(
    LangKeyedSource source,
    List<TranslatedLangKeyed<?>> translatedOutput,
    Map<Object, String> translationByWrappedOutput
  ) throws IllegalStateException {
    var buckets = new HashMap<String, ArrayList<LangKeyed<?>>>();

    for (var langKeyed : source.items()) {
      var translationValue = getTranslationOrNull(langKeyed);

      if (translationValue == null) {
        logger.warning("Could not locate translation-value for key " + langKeyed.getLanguageFileKey());
        continue;
      }

      translationByWrappedOutput.put(langKeyed.getWrapped(), translationValue);

      var normalizedTranslationValue = TranslatedLangKeyed.normalize(translationValue);

      var bucket = buckets.computeIfAbsent(normalizedTranslationValue, k -> new ArrayList<>());
      bucket.add(langKeyed);
    }

    for (var bucketEntry : buckets.entrySet()) {
      var bucketNormalizedUnPrefixedTranslation = bucketEntry.getKey();
      var bucketItems = bucketEntry.getValue();
      var bucketSize = bucketItems.size();

      // Prefix all items of other sources that would collide with the item about to be added
      for (var itemIndex = 0; itemIndex < bucketSize; ++itemIndex) {
        var bucketItem = bucketItems.get(itemIndex);

        boolean hadCollision = false;

        for (var outputIndex = 0; outputIndex < translatedOutput.size(); ++outputIndex) {
          var existingEntry = translatedOutput.get(outputIndex);

          // Do not add cross-source collision prefixes on same-source items, as the incrementing
          // bucket index already takes care of these kinds of collision
          if (existingEntry.source == source)
            continue;

          if (!existingEntry.normalizedUnPrefixedTranslation.equalsIgnoreCase(bucketNormalizedUnPrefixedTranslation))
            continue;

          translatedOutput.set(outputIndex, new TranslatedLangKeyed<>(
            existingEntry.source,
            existingEntry.langKeyed,
            existingEntry.normalizedUnPrefixedTranslation,
            existingEntry.source.collisionPrefix() + existingEntry.normalizedPrefixedTranslation
          ));

          hadCollision = true;
        }

        var newItemPrefixedTranslation = bucketNormalizedUnPrefixedTranslation;

        // Incrementing same-source prefixes should be nearest to the translation
        if (bucketSize > 1)
          newItemPrefixedTranslation = "[" + (itemIndex + 1) + "]-" + newItemPrefixedTranslation;

        if (hadCollision)
          newItemPrefixedTranslation = source.collisionPrefix() + newItemPrefixedTranslation;

        translatedOutput.add(new TranslatedLangKeyed<>(source, bucketItem, bucketNormalizedUnPrefixedTranslation, newItemPrefixedTranslation));
      }
    }
  }

  private @Nullable String accessLanguageKey(String key, LangKeyed<?> langKeyed) {
    var translationValue = languageFile.get(key);

    if (translationValue == null) {
      if (langKeyed != null && translationResolver != null)
        return translationResolver.resolve(langKeyed);
      return null;
    }

    if (!(translationValue instanceof JsonPrimitive))
      return null;

    return translationValue.getAsString();
  }

  private String normalizeDescriptionTranslation(String descriptionTranslation) {
    return descriptionTranslation.replace(" - ", "-");
  }

  private @Nullable String tryGetSmithingTemplateDescriptionKey(LangKeyed<?> langKeyed) {
    /*
      The upgrade seems to be a completely different key:
      item.minecraft.>netherite_upgrade<_smithing_template => upgrade.minecraft.>netherite_upgrade<
     */

    if (langKeyed.getWrapped() instanceof Material material && material.name().equals("NETHERITE_UPGRADE_SMITHING_TEMPLATE"))
      return "upgrade.minecraft.netherite_upgrade";

    return null;
  }

  private @Nullable String getTranslationOrNull(LangKeyed<?> langKeyed) {
    var fileKey = langKeyed.getLanguageFileKey();

    if (langKeyed.getWrapped() instanceof Material) {
      String descriptionTranslationKey = tryGetSmithingTemplateDescriptionKey(langKeyed);

      if (descriptionTranslationKey == null)
        descriptionTranslationKey = fileKey + ".desc";

      var descriptionTranslation = accessLanguageKey(descriptionTranslationKey, null);

      if (descriptionTranslation != null)
        return accessLanguageKey(fileKey, langKeyed) + " " + normalizeDescriptionTranslation(descriptionTranslation);
    }

    return accessLanguageKey(fileKey, langKeyed);
  }
}