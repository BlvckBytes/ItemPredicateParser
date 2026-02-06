package me.blvckbytes.item_predicate_parser.parse;

import me.blvckbytes.item_predicate_parser.token.*;
import me.blvckbytes.item_predicate_parser.predicate.*;
import me.blvckbytes.item_predicate_parser.translation.*;
import me.blvckbytes.item_predicate_parser.translation.keyed.*;
import me.blvckbytes.syllables_matcher.WildcardMode;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class PredicateParser {

  @FunctionalInterface
  private interface BinaryNodeConstructor {
    ItemPredicate call(Token token, TranslatedLangKeyed<?> langKeyed, ItemPredicate lhs, ItemPredicate rhs);
  }

  @FunctionalInterface
  private interface UnaryNodeConstructor {
    ItemPredicate call(Token token, TranslatedLangKeyed<?> langKeyed, ItemPredicate operand);
  }

  private final TranslationRegistry translationRegistry;
  private final TranslatedLangKeyed<?> conjunctionTranslation;
  private final ArrayList<Token> tokens;
  private final Map<Token, TranslatedLangKeyed<?>> resolveCache;
  private final boolean allowMissingClosingParentheses;

  public PredicateParser(
    TranslationRegistry translationRegistry,
    TranslatedLangKeyed<?> conjunctionTranslation,
    ArrayList<Token> tokens,
    boolean allowMissingClosingParentheses
  ) {
    this.translationRegistry = translationRegistry;
    this.tokens = tokens;
    this.allowMissingClosingParentheses = allowMissingClosingParentheses;
    this.conjunctionTranslation = conjunctionTranslation;
    this.resolveCache = new HashMap<>();
  }

  public @Nullable ItemPredicate parseAst() {
    var result = parseDisjunctionNode();

    while (!tokens.isEmpty()) {
      var nextExpression = parseDisjunctionNode();

      if (nextExpression == null)
        break;

      // Consecutive predicates are implicitly joined by AND
      result = new ConjunctionNode(null, conjunctionTranslation, result, nextExpression);
    }

    if (!tokens.isEmpty())
      throw new ItemPredicateParseException(tokens.remove(0), ParseConflict.EXPECTED_LEFT_HAND_SIDE);

    return result;
  }

  private @Nullable ItemPredicate parseConjunctionNode() {
    return parseBinaryNode(this::parseNegationNode, ConjunctionKey.class, ConjunctionNode::new);
  }

  private @Nullable ItemPredicate parseDisjunctionNode() {
    return parseBinaryNode(this::parseConjunctionNode, DisjunctionKey.class, DisjunctionNode::new);
  }

  private @Nullable ItemPredicate parseBinaryNode(
    Supplier<ItemPredicate> parser,
    Class<? extends LangKeyed<?>> operatorType,
    BinaryNodeConstructor constructor
  ) {
    var result = parser.get();

    if (result == null)
      return null;

    while (!tokens.isEmpty()) {
      var token = tokens.get(0);
      var translated = resolveTranslated(token);

      if (translated == null || !operatorType.isInstance(translated.langKeyed))
        break;

      tokens.remove(0);

      var rhs = parser.get();

      if (rhs == null)
        throw new ItemPredicateParseException(token, ParseConflict.EXPECTED_EXPRESSION_AFTER_OPERATOR);

      result = constructor.call(token, translated, result, rhs);
    }

    return result;
  }

  private @Nullable ItemPredicate parseNegationNode() {
    return parseUnaryNode(this::parseInnerAllNode, NegationKey.class, NegationNode::new);
  }

  private @Nullable ItemPredicate parseInnerAllNode() {
    return parseUnaryNode(this::parseInnerSomeNode, InnerAllKey.class, InnerAllNode::new);
  }

  private @Nullable ItemPredicate parseInnerSomeNode() {
    return parseUnaryNode(this::parseExactNode, InnerSomeKey.class, InnerSomeNode::new);
  }

  private @Nullable ItemPredicate parseExactNode() {
    return parseUnaryNode(this::parseParenthesesNode, ExactKey.class, ExactNode::new);
  }

  private @Nullable ItemPredicate parseUnaryNode(
    Supplier<ItemPredicate> parser,
    Class<? extends LangKeyed<?>> operatorType,
    UnaryNodeConstructor constructor
  ) {
    if (tokens.isEmpty())
      return null;

    var token = tokens.get(0);
    var translated = resolveTranslated(token);

    if (translated == null || !operatorType.isInstance(translated.langKeyed))
      return parser.get();

    tokens.remove(0);

    var operand = parser.get();

    if (operand == null)
      throw new ItemPredicateParseException(token, ParseConflict.EXPECTED_EXPRESSION_AFTER_OPERATOR);

    return constructor.call(token, translated, operand);
  }

  private @Nullable ItemPredicate parseParenthesesNode() {
    if (tokens.isEmpty())
      return null;

    var token = tokens.get(0);

    if (!(token instanceof ParenthesisToken openingToken))
      return parseItemPredicate();

    if (!openingToken.isOpening())
      throw new ItemPredicateParseException(token, ParseConflict.EXPECTED_OPENING_PARENTHESIS);

    tokens.remove(0);

    // This check provides better user-experience, as an empty pair of parentheses would yield the following behavior:
    // The ( enters a new ParenthesesNode, which re-climbs the precedence ladder.
    // The next invocation will expect ( but gets ) and thus throws a parentheses-mismatch.
    // This way, it becomes clear that the content within the pair is what's actually missing.
    if (!tokens.isEmpty() && tokens.get(0) instanceof ParenthesisToken parenthesisToken && !parenthesisToken.isOpening())
      throw new ItemPredicateParseException(token, ParseConflict.EXPECTED_SEARCH_PATTERN);

    // Invoke the lowest precedence parser
    var inner = parseDisjunctionNode();

    if (inner == null)
      throw new ItemPredicateParseException(token, ParseConflict.EXPECTED_SEARCH_PATTERN);

    // Takes care of parentheses which immediately followed the next higher precedence inside the current
    // parentheses - example: (dia-ches (unbr)). The ( of (unbr) would be left by the predicate parser,
    // and so the parentheses parser needs to pick it up and implicitly add conjunctions to inner
    while (!tokens.isEmpty()) {
      if (!((tokens.get(0) instanceof ParenthesisToken nextToken) && nextToken.isOpening()))
        break;

      inner = new ConjunctionNode(null, conjunctionTranslation, inner, parseParenthesesNode());
    }

    if (tokens.isEmpty()) {
      if (allowMissingClosingParentheses)
        return new ParenthesesNode(inner);

      throw new ItemPredicateParseException(token, ParseConflict.EXPECTED_CLOSING_PARENTHESIS);
    }

    token = tokens.get(0);

    if (!(token instanceof ParenthesisToken closingToken) || closingToken.isOpening()) {
      if (allowMissingClosingParentheses) {
        while (!tokens.isEmpty()) {
          // Found corresponding closing parenthesis
          if (tokens.get(0) instanceof ParenthesisToken nextToken && !nextToken.isOpening()) {
            tokens.remove(0);
            break;
          }

          // Re-climb the precedence ladder
          // This way, missing closing parentheses will be added to the very end, which makes it
          // possible to actually get the desired result, until closing-parens are added in while typing
          inner = new ConjunctionNode(null, conjunctionTranslation, inner, parseDisjunctionNode());
        }

        return new ParenthesesNode(inner);
      }

      throw new ItemPredicateParseException(token, ParseConflict.EXPECTED_CLOSING_PARENTHESIS);
    }

    tokens.remove(0);

    return new ParenthesesNode(inner);
  }

  private @Nullable TranslatedLangKeyed<?> resolveTranslated(Token token) {
    if (!(token instanceof UnquotedStringToken stringToken))
      return null;

    return resolveCache.computeIfAbsent(token, tk -> {
      var searchResult = translationRegistry.search(stringToken);

      if (searchResult.wildcardMode() != WildcardMode.NONE)
        return null;

      return getShortestMatch(searchResult.result());
    });
  }

  @SuppressWarnings("unchecked")
  private @Nullable ItemPredicate parseItemPredicate() {
    var predicates = new ArrayList<ItemPredicate>();

    while (!tokens.isEmpty()) {
      var currentToken = tokens.get(0);

      if (currentToken instanceof QuotedStringToken textSearch) {
        predicates.add(new TextSearchPredicate(textSearch));
        tokens.remove(0);
        continue;
      }

      if (currentToken instanceof ParenthesisToken)
        break;

      if (!(currentToken instanceof UnquotedStringToken translationSearch))
        throw new ItemPredicateParseException(currentToken, ParseConflict.EXPECTED_SEARCH_PATTERN);

      var searchResult = translationRegistry.search(translationSearch);
      var searchResultEntries = searchResult.result();

      // Wildcards may only apply to materials, not only because that's the only place where they make sense, but
      // because otherwise, predicate-ambiguity would arise.
      if (searchResult.wildcardMode() != WildcardMode.NONE) {
        var materials = new ArrayList<Material>();

        for (var resultEntry : searchResultEntries) {
          if (resultEntry.langKeyed.getWrapped() instanceof Material material)
            materials.add(material);
        }

        if (materials.isEmpty())
          throw new ItemPredicateParseException(currentToken, ParseConflict.NO_SEARCH_MATCH);

        predicates.add(new MaterialPredicate(translationSearch, null, materials));
        tokens.remove(0);
        continue;
      }

      var shortestMatch = getShortestMatch(searchResultEntries);

      if (shortestMatch == null)
        throw new ItemPredicateParseException(currentToken, ParseConflict.NO_SEARCH_MATCH);

      ItemPredicate predicate;

      switch (shortestMatch.langKeyed.getPredicateType()) {
        case ITEM_MATERIAL -> {
          predicates.add(new MaterialPredicate(translationSearch, (TranslatedLangKeyed<LangKeyedItemMaterial>) shortestMatch, null));
          tokens.remove(0);
          continue;
        }
        case ENCHANTMENT -> {
          tokens.remove(0);

          IntegerToken enchantmentLevel = tryConsumeIntegerArgument(tokens);
          throwOnTimeNotation(enchantmentLevel);

          predicates.add(new EnchantmentPredicate(currentToken, (TranslatedLangKeyed<LangKeyedEnchantment>) shortestMatch, enchantmentLevel));
          continue;
        }
        case POTION_EFFECT_TYPE -> {
          tokens.remove(0);

          IntegerToken potionEffectAmplifier = tryConsumeIntegerArgument(tokens);
          throwOnTimeNotation(potionEffectAmplifier);

          IntegerToken potionEffectDuration = tryConsumeIntegerArgument(tokens);

          predicates.add(new PotionEffectPredicate(currentToken, (TranslatedLangKeyed<LangKeyedPotionEffectType>) shortestMatch, potionEffectAmplifier, potionEffectDuration));
          continue;
        }
        case POTION_TYPE -> {
          tokens.remove(0);
          predicates.add(new PotionTypePredicate(currentToken, (TranslatedLangKeyed<LangKeyedPotionType>) shortestMatch));
          continue;
        }
        case DETERIORATION -> {
          tokens.remove(0);

          IntegerToken deteriorationPercentageMin = tryConsumeIntegerArgument(tokens);
          throwOnTimeNotation(deteriorationPercentageMin);
          throwOnNonEqualsComparison(deteriorationPercentageMin);

          IntegerToken deteriorationPercentageMax = tryConsumeIntegerArgument(tokens);
          throwOnTimeNotation(deteriorationPercentageMax);
          throwOnNonEqualsComparison(deteriorationPercentageMax);

          predicates.add(new DeteriorationPredicate(currentToken, (TranslatedLangKeyed<DeteriorationKey>) shortestMatch, deteriorationPercentageMin, deteriorationPercentageMax));
          continue;
        }
        case AMOUNT -> {
          tokens.remove(0);

          IntegerToken amount = tryConsumeIntegerArgument(tokens);
          throwOnTimeNotation(amount);

          if (amount == null || amount.value() == null)
            throw new ItemPredicateParseException(currentToken, ParseConflict.EXPECTED_FOLLOWING_INTEGER);

          predicates.add(new AmountPredicate(currentToken, (TranslatedLangKeyed<AmountKey>) shortestMatch, amount));
          continue;
        }
        case MUSIC_INSTRUMENT -> {
          tokens.remove(0);

          if ((predicate = translationRegistry.getVersionDependentCode().makeInstrumentPredicate(currentToken, shortestMatch)) != null)
            predicates.add(predicate);

          continue;
        }
        case VARIABLE -> {
          tokens.remove(0);
          predicates.add(new VariablePredicate(currentToken, (TranslatedLangKeyed<VariableKey>) shortestMatch));
          continue;
        }
        case ANY -> {
          tokens.remove(0);
          predicates.add(new AnyPredicate(currentToken, (TranslatedLangKeyed<AnyKey>) shortestMatch));
          continue;
        }
        case HAS_NAME -> {
          tokens.remove(0);
          predicates.add(new HasNamePredicate(currentToken, (TranslatedLangKeyed<HasNameKey>) shortestMatch));
          continue;
        }
        default -> {}
      }

      break;
    }

    if (predicates.isEmpty())
      return null;

    ItemPredicate result = predicates.remove(0);

    // Consecutive predicates are implicitly joined by AND
    while (!predicates.isEmpty())
      result = new ConjunctionNode(null, conjunctionTranslation, result, predicates.remove(0));

    return result;
  }

  private static void throwOnNonEqualsComparison(@Nullable IntegerToken token) {
    if (token == null)
      return;

    if (token.comparisonMode() == ComparisonMode.EQUALS)
      return;

    throw new ItemPredicateParseException(token, ParseConflict.DOES_NOT_ACCEPT_NON_EQUALS_COMPARISON);
  }

  private static void throwOnTimeNotation(@Nullable IntegerToken token) {
    if (token == null)
      return;

    if (!token.wasTimeNotation())
      return;

    throw new ItemPredicateParseException(token, ParseConflict.DOES_NOT_ACCEPT_TIME_NOTATION);
  }

  private static @Nullable IntegerToken tryConsumeIntegerArgument(List<Token> tokens) {
    IntegerToken integerToken = null;

    if (!tokens.isEmpty()) {
      var nextToken = tokens.get(0);

      if (nextToken instanceof IntegerToken argument) {
        integerToken = argument;
        tokens.remove(0);
      }
    }

    return integerToken;
  }

  private static @Nullable TranslatedLangKeyed<?> getShortestMatch(List<TranslatedLangKeyed<?>> matches) {
    if (matches.isEmpty())
      return null;

    var numberOfMatches = matches.size();

    if (numberOfMatches == 1)
      return matches.get(0);

    var shortestMatchLength = Integer.MAX_VALUE;
    TranslatedLangKeyed<?> shortestMatch = null;

    for (TranslatedLangKeyed<?> currentMatch : matches) {
      var currentLength = currentMatch.normalizedPrefixedTranslation.length();

      if (currentLength > shortestMatchLength)
        continue;

      if (shortestMatch != null && currentLength == shortestMatchLength) {
        if (currentMatch.alphabeticalIndex < shortestMatch.alphabeticalIndex)
          shortestMatch = currentMatch;

        continue;
      }

      shortestMatchLength = currentLength;
      shortestMatch = currentMatch;
    }

    return shortestMatch;
  }
}
