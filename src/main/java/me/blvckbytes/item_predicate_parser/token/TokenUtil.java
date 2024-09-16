package me.blvckbytes.item_predicate_parser.token;

import me.blvckbytes.item_predicate_parser.translation.TranslationRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TokenUtil {

  private static final UnquotedStringToken EMPTY_SEARCH = new UnquotedStringToken(0, 0, null, "");

  public static @Nullable List<String> createSuggestions(
    TranslationRegistry registry,
    int maxResults,
    Function<Integer, String> moreResultsLineGenerator,
    List<Token> tokens
  ) {
    if (tokens.isEmpty())
      return executeSearch(registry, maxResults, moreResultsLineGenerator, EMPTY_SEARCH, "");

    var args = tokens.getFirst().parserInput().getInputAsArguments();
    var lastArgIndex = args.length - 1;

    if (args[lastArgIndex].isEmpty())
      return executeSearch(registry, maxResults, moreResultsLineGenerator, EMPTY_SEARCH, "");

    var argCorrespondingTokens = new ArrayList<Token>();

    for (var tokenIndex = tokens.size() - 1; tokenIndex >= 0; --tokenIndex) {
      var currentToken = tokens.get(tokenIndex);

      if (currentToken.beginCommandArgumentIndex() < lastArgIndex)
        break;

      if (currentToken.endCommandArgumentIndex() > lastArgIndex)
        continue;

      argCorrespondingTokens.addFirst(currentToken);
    }

    if (argCorrespondingTokens.isEmpty())
      return null;

    var lastToken = argCorrespondingTokens.getLast();

    if (!(lastToken instanceof UnquotedStringToken search))
      return null;

    var resultPrefixBuilder = new StringBuilder();

    for (var index = 0; index < argCorrespondingTokens.size() - 1; ++index) {
      var previousToken = argCorrespondingTokens.get(index);
      resultPrefixBuilder.append(previousToken.stringify());
    }

    return executeSearch(registry, maxResults, moreResultsLineGenerator, search, resultPrefixBuilder.toString());
  }

  private static List<String> executeSearch(
    TranslationRegistry registry,
    int maxResults,
    Function<Integer, String> moreResultsLineGenerator,
    UnquotedStringToken search,
    String itemPrefix
  ) {
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

    if (resultCount > maxResults)
      resultTexts.add(moreResultsLineGenerator.apply(resultCount - maxResults));

    return resultTexts;
  }
}
