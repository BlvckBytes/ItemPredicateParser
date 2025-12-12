package me.blvckbytes.item_predicate_parser;

import me.blvckbytes.bbconfigmapper.ScalarType;
import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import me.blvckbytes.gpeee.GPEEE;
import me.blvckbytes.item_predicate_parser.config.MainSection;
import me.blvckbytes.item_predicate_parser.parse.ItemPredicateParseException;
import me.blvckbytes.item_predicate_parser.parse.PredicateParser;
import me.blvckbytes.item_predicate_parser.parse.TokenParser;
import me.blvckbytes.item_predicate_parser.predicate.ItemPredicate;
import me.blvckbytes.item_predicate_parser.predicate.StringifyState;
import me.blvckbytes.item_predicate_parser.token.Token;
import me.blvckbytes.item_predicate_parser.token.UnquotedStringToken;
import me.blvckbytes.item_predicate_parser.translation.LanguageRegistry;
import me.blvckbytes.item_predicate_parser.translation.TranslatedLangKeyed;
import me.blvckbytes.item_predicate_parser.translation.TranslationLanguage;
import me.blvckbytes.item_predicate_parser.translation.TranslationRegistry;
import me.blvckbytes.item_predicate_parser.translation.keyed.ConjunctionKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PredicateHelper {

  private static final UnquotedStringToken EMPTY_STRING = new UnquotedStringToken(0, 0, null, "");

  private final NameScopedKeyValueStore keyValueStore;
  private final LanguageRegistry languageRegistry;
  private final ConfigKeeper<MainSection> config;

  private final Map<TranslationLanguage, TranslatedLangKeyed<?>> conjunctionTranslations;
  private int maxResults;

  public PredicateHelper(
    NameScopedKeyValueStore keyValueStore,
    LanguageRegistry languageRegistry,
    ConfigKeeper<MainSection> config
  ) {
    this.keyValueStore = keyValueStore;
    this.languageRegistry = languageRegistry;
    this.config = config;
    this.conjunctionTranslations = new HashMap<>();

    // Conjunction translations are required when parsing to inject implicitly

    for (var language : TranslationLanguage.values()) {
      var conjunctionTranslation = languageRegistry.getTranslationRegistry(language).lookup(ConjunctionKey.INSTANCE);

      if (conjunctionTranslation == null)
        throw new IllegalStateException("Expected the " + language.assetFileNameWithoutExtension + "-registry to know about the conjunction translation");

      conjunctionTranslations.put(language, conjunctionTranslation);
    }

    updateMaxResults();

    config.registerReloadListener(this::updateMaxResults);
  }

  public TranslationLanguage getSelectedLanguage(Player player) {
    var languageName = keyValueStore.read(player.getUniqueId().toString(), NameScopedKeyValueStore.KEY_LANGUAGE);

    if (languageName == null)
      return config.rootSection.defaultSelectedLanguage;

    try {
      return TranslationLanguage.valueOf(languageName);
    } catch (Throwable e) {
      return config.rootSection.defaultSelectedLanguage;
    }
  }

  private void updateMaxResults() {
    maxResults = config.rootSection.maxCompletionsCount.asScalar(ScalarType.INT, config.rootSection.builtBaseEnvironment);
  }

  /**
   * Parse tokens from a command's argument-array
   * @param args Argument-array of command
   * @param offset Index at which the predicate begins (including)
   * @return List of parsed tokens; empty on blank input
   * @throws ItemPredicateParseException Various errors during the parsing process
   */
  public List<Token> parseTokens(String[] args, int offset) throws ItemPredicateParseException {
    return TokenParser.parseTokens(args, offset);
  }

  /**
   * Parse tokens from a plain string - mainly used when reviving persisted predicates
   * @param text Input string
   * @return List of parsed tokens; empty on blank input
   * @throws ItemPredicateParseException Various errors during the parsing process
   */
  public List<Token> parseTokens(String text) throws ItemPredicateParseException {
    return TokenParser.parseTokens(text);
  }

  /**
   * Parse a predicate based on a list of previously parsed tokens
   * @param language Language whose identifiers are to be matched against
   * @param tokens Previously parsed tokens; See {@link #parseTokens(String[], int)} and {@link #parseTokens(String)}
   * @return Null on an empty list of tokens; a predicate on success
   * @throws ItemPredicateParseException Various errors during the parsing process
   */
  public @Nullable ItemPredicate parsePredicate(TranslationLanguage language, List<Token> tokens) throws ItemPredicateParseException {
    return _parsePredicate(language, tokens, false);
  }

  /**
   * Create both the list of suggestions and the expanded preview based on a list of previously parsed tokens
   * @param language Language whose identifiers are to be matched against
   * @param tokens Previously parsed tokens; See {@link #parseTokens(String[], int)} and {@link #parseTokens(String)}
   * @throws ItemPredicateParseException Various errors during the parsing process
   */
  public CompletionResult createCompletion(TranslationLanguage language, List<Token> tokens) throws ItemPredicateParseException {
    String expandedPreviewOrError = null;
    boolean didParseErrorOccur = false;

    try {
      var predicate = _parsePredicate(language, tokens, true);

      if (predicate != null && config.rootSection.expandedPreview != null) {
        expandedPreviewOrError = config.rootSection.expandedPreview.asScalar(
          ScalarType.STRING,
          config.rootSection.getBaseEnvironment()
            .withStaticVariable(
              "predicate_representation",
              new StringifyState(false).appendPredicate(predicate).toString()
            )
            .build()
        );
      }
    } catch (ItemPredicateParseException e) {
      didParseErrorOccur = true;
      expandedPreviewOrError = createExceptionMessage(e);
    }

    return new CompletionResult(
      createSuggestions(languageRegistry.getTranslationRegistry(language), tokens),
      expandedPreviewOrError,
      didParseErrorOccur
    );
  }

  /**
   * Creates an exception message as configured within the config, by highlighting user-input and
   * attaching a conflict-specific description message
   */
  public String createExceptionMessage(ItemPredicateParseException exception) {
    String highlightPrefix = "", nonHighlightPrefix = "";

    if (config.rootSection.inputHighlightPrefix != null)
      highlightPrefix = config.rootSection.inputHighlightPrefix.asScalar(ScalarType.STRING, GPEEE.EMPTY_ENVIRONMENT);

    if (config.rootSection.inputNonHighlightPrefix != null)
      nonHighlightPrefix = config.rootSection.inputNonHighlightPrefix.asScalar(ScalarType.STRING, GPEEE.EMPTY_ENVIRONMENT);

    var highlightedInput = exception.highlightedInput(nonHighlightPrefix, highlightPrefix);

    var conflictEvaluable = config.rootSection.parseConflicts.get(exception.getConflict().name());

    if (conflictEvaluable == null)
      return highlightedInput;

    return conflictEvaluable.asScalar(
      ScalarType.STRING,
      config.rootSection.getBaseEnvironment()
        .withStaticVariable("highlighted_input", highlightedInput)
        .build()
    );
  }

  private @Nullable ItemPredicate _parsePredicate(TranslationLanguage language, List<Token> tokens, boolean allowMissingClosingParentheses) {
    return new PredicateParser(
      languageRegistry.getTranslationRegistry(language),
      conjunctionTranslations.get(language),
      new ArrayList<>(tokens), allowMissingClosingParentheses
    ).parseAst();
  }

  private @Nullable List<String> createSuggestions(TranslationRegistry registry, List<Token> tokens) {
    if (tokens.isEmpty())
      return executeSearch(registry, EMPTY_STRING, "");

    var args = tokens.get(0).parserInput().getInputAsArguments();
    var lastArgIndex = args.length - 1;

    if (args[lastArgIndex].isEmpty())
      return executeSearch(registry, EMPTY_STRING, "");

    var argCorrespondingTokens = new ArrayList<Token>();

    for (var tokenIndex = tokens.size() - 1; tokenIndex >= 0; --tokenIndex) {
      var currentToken = tokens.get(tokenIndex);

      if (currentToken.beginCommandArgumentIndex() < lastArgIndex)
        break;

      if (currentToken.endCommandArgumentIndex() > lastArgIndex)
        continue;

      argCorrespondingTokens.add(0, currentToken);
    }

    if (argCorrespondingTokens.isEmpty())
      return null;

    var lastToken = argCorrespondingTokens.get(argCorrespondingTokens.size() - 1);

    if (!(lastToken instanceof UnquotedStringToken search))
      return null;

    var resultPrefixBuilder = new StringBuilder();

    for (var index = 0; index < argCorrespondingTokens.size() - 1; ++index) {
      var previousToken = argCorrespondingTokens.get(index);
      resultPrefixBuilder.append(previousToken.stringify());
    }

    return executeSearch(registry, search, resultPrefixBuilder.toString());
  }

  private List<String> executeSearch(TranslationRegistry registry, UnquotedStringToken search, String itemPrefix) {
    var searchResult = registry.search(search);
    var resultItems = searchResult.result();
    var resultCount = resultItems.size();

    resultItems.sort((a, b) -> {
      var aLength = a.normalizedPrefixedTranslation.length();
      var bLength = b.normalizedPrefixedTranslation.length();

      if (aLength != bLength)
        return aLength - bLength;

      return a.alphabeticalIndex - b.alphabeticalIndex;
    });

    if (resultCount > maxResults)
      resultItems = resultItems.subList(0, maxResults);

    var resultTexts = resultItems
      .stream()
      .map(it -> itemPrefix + it.normalizedPrefixedTranslation)
      .collect(Collectors.toList());

    if (resultCount > maxResults && config.rootSection.maxCompletionsExceeded != null) {
      resultTexts.add(config.rootSection.maxCompletionsExceeded.asScalar(
        ScalarType.STRING,
        config.rootSection.getBaseEnvironment()
          .withStaticVariable("excess_count", resultCount - maxResults)
          .build()
      ));
    }

    return resultTexts;
  }
}