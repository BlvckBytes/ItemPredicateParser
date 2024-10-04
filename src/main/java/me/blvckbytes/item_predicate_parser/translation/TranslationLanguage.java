package me.blvckbytes.item_predicate_parser.translation;

import me.blvckbytes.item_predicate_parser.parse.SubstringIndices;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public enum TranslationLanguage {

  // NOTE: There is a reason as to why only select languages are supported, and their collision-prefixes
  //       as well as their custom-key-translations are hardcoded: Firstly, expressions shall behave
  //       uniformly across all consumers of this API, and secondly, I want to make sure that there are
  //       no parsing- or comparison-errors when it comes to Unicode-symbols.

  ENGLISH_US("en_us", CollisionPrefixes.ENGLISH, CustomTranslations.ENGLISH),
  ENGLISH_GB("en_gb", CollisionPrefixes.ENGLISH, CustomTranslations.ENGLISH),
  CHINESE_CN("zh_cn", CollisionPrefixes.CHINESE_SIMPLIFIED, CustomTranslations.CHINESE_SIMPLIFIED),
  GERMAN_DE("de_de", CollisionPrefixes.GERMAN, CustomTranslations.GERMAN),
  GERMAN_AT("de_at", CollisionPrefixes.GERMAN, CustomTranslations.GERMAN),
  GERMAN_CH("de_ch", CollisionPrefixes.GERMAN, CustomTranslations.GERMAN),

  ;

  private static final TranslationLanguage[] sortedValues;

  static {
    sortedValues = Arrays.stream(values())
      .sorted(Comparator.comparing(a -> a.normalizedName))
      .toArray(TranslationLanguage[]::new);
  }

  public final String assetFileNameWithoutExtension;
  public final CollisionPrefixes collisionPrefixes;
  public final CustomTranslations customTranslations;
  public final String normalizedName;
  private final List<SubstringIndices> normalizedNameIndices;

  TranslationLanguage(
    String assetFileNameWithoutExtension,
    CollisionPrefixes collisionPrefixes,
    CustomTranslations customTranslations
  ) {
    this.assetFileNameWithoutExtension = assetFileNameWithoutExtension;
    this.collisionPrefixes = collisionPrefixes;
    this.customTranslations = customTranslations;
    this.normalizedName = normalizeName(name());
    this.normalizedNameIndices = SubstringIndices.forString(null, this.normalizedName, '-');
  }

  public static List<String> createCompletions(String input) {
    var result = new ArrayList<String>();

    forEachMatch(input, match -> result.add(match.normalizedName));

    return result;
  }

  public static @Nullable TranslationLanguage matchFirst(String input) {
    return forEachMatch(input, match -> false);
  }

  private static @Nullable TranslationLanguage forEachMatch(String input, Function<TranslationLanguage, Boolean> matchHandler) {
    var inputIndices = SubstringIndices.forString(null, input, '-');

    for (var translationLanguage : sortedValues) {
      var pendingInputSubstrings = new ArrayList<>(inputIndices);

      SubstringIndices.matchQuerySubstrings(
        input, pendingInputSubstrings,
        translationLanguage.normalizedName, new ArrayList<>(translationLanguage.normalizedNameIndices)
      );

      if (pendingInputSubstrings.isEmpty()) {
        if (!matchHandler.apply(translationLanguage))
          return translationLanguage;
      }
    }

    return null;
  }

  private static String normalizeName(String name) {
    var result = new StringBuilder();
    char previousChar = 0;

    for (var charIndex = 0; charIndex < name.length(); ++charIndex) {
      var currentChar = name.charAt(charIndex);

      if (currentChar == '_')
        result.append('-');
      else if (charIndex == 0 || previousChar == '-' || previousChar == '_')
        result.append(Character.toUpperCase(currentChar));
      else
        result.append(Character.toLowerCase(currentChar));

      previousChar = currentChar;
    }

    return result.toString();
  }
}
