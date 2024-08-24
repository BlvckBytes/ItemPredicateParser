package me.blvckbytes.storage_query.parse;

import me.blvckbytes.storage_query.token.IntegerToken;
import me.blvckbytes.storage_query.token.QuotedStringToken;
import me.blvckbytes.storage_query.token.Token;
import me.blvckbytes.storage_query.token.UnquotedStringToken;
import me.blvckbytes.storage_query.predicate.*;
import me.blvckbytes.storage_query.translation.DeteriorationKey;
import me.blvckbytes.storage_query.translation.TranslatedTranslatable;
import me.blvckbytes.storage_query.translation.TranslationRegistry;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PredicateParser {

  public static List<ItemPredicate> parsePredicates(List<Token> tokens, TranslationRegistry registry) {
    var result = new ArrayList<ItemPredicate>();
    var remainingTokens = new ArrayList<>(tokens);

    while (!remainingTokens.isEmpty()) {
      var currentToken = remainingTokens.removeFirst();

      if (currentToken instanceof QuotedStringToken textSearch) {
        result.add(new TextSearchPredicate(textSearch.value()));
        continue;
      }

      if (!(currentToken instanceof UnquotedStringToken translationSearch))
        throw new ArgumentParseException(currentToken.getCommandArgumentIndex(), ParseConflict.EXPECTED_SEARCH_PATTERN);

      var searchString = translationSearch.value();

      if (searchString.isEmpty())
        continue;

      var searchResults = registry.search(searchString);
      var shortestMatch = getShortestMatch(searchResults);

      if (shortestMatch == null)
        throw new ArgumentParseException(((UnquotedStringToken) currentToken).commandArgumentIndex(), ParseConflict.NO_SEARCH_MATCH);

      if (shortestMatch.translatable() instanceof Material predicateMaterial) {
        result.add(new MaterialPredicate(shortestMatch, predicateMaterial));
        continue;
      }

      if (shortestMatch.translatable() instanceof Enchantment predicateEnchantment) {
        IntegerToken enchantmentLevel = tryConsumeIntegerArgument(remainingTokens);
        result.add(new EnchantmentPredicate(shortestMatch, predicateEnchantment, enchantmentLevel));
        continue;
      }

      if (shortestMatch.translatable() instanceof PotionEffectType predicatePotionEffect) {
        IntegerToken potionEffectAmplifier = tryConsumeIntegerArgument(remainingTokens);
        IntegerToken potionEffectDuration = tryConsumeIntegerArgument(remainingTokens);
        result.add(new PotionEffectPredicate(shortestMatch, predicatePotionEffect, potionEffectAmplifier, potionEffectDuration));
        continue;
      }

      if (shortestMatch.translatable() instanceof DeteriorationKey) {
        IntegerToken deteriorationPercentageMin = tryConsumeIntegerArgument(remainingTokens);
        IntegerToken deteriorationPercentageMax = tryConsumeIntegerArgument(remainingTokens);

        // I think that it'll be friendlier to act out on a no-op, rather than to throw an error
        // By falling back to a wildcard, the user is also shown that there are parameters, in the expanded form

        if (deteriorationPercentageMin == null)
          deteriorationPercentageMin = new IntegerToken(currentToken.getCommandArgumentIndex(), null);

        if (deteriorationPercentageMax == null)
          deteriorationPercentageMax = new IntegerToken(currentToken.getCommandArgumentIndex(), null);

        result.add(new DeteriorationPredicate(shortestMatch, deteriorationPercentageMin, deteriorationPercentageMax));
        continue;
      }

      throw new ArgumentParseException(currentToken.getCommandArgumentIndex(), ParseConflict.UNIMPLEMENTED_TRANSLATABLE);
    }

    return result;
  }

  private static @Nullable IntegerToken tryConsumeIntegerArgument(List<Token> tokens) {
    IntegerToken integerToken = null;

    if (!tokens.isEmpty()) {
      var nextToken = tokens.getFirst();

      if (nextToken instanceof IntegerToken argument) {
        integerToken = argument;
        tokens.removeFirst();
      }
    }

    return integerToken;
  }

  private static @Nullable TranslatedTranslatable getShortestMatch(List<TranslatedTranslatable> matches) {
    if (matches.isEmpty())
      return null;

    var numberOfMatches = matches.size();

    if (numberOfMatches == 1)
      return matches.getFirst();

    var shortestMatchLength = Integer.MAX_VALUE;
    var shortestMatchIndex = 0;

    for (var matchIndex = 0; matchIndex < numberOfMatches; ++matchIndex) {
      var currentLength = matches.get(matchIndex).translation().length();

      if (currentLength < shortestMatchLength) {
        shortestMatchLength = currentLength;
        shortestMatchIndex = matchIndex;
      }
    }

    return matches.get(shortestMatchIndex);
  }
}
