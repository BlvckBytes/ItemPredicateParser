package me.blvckbytes.item_predicate_parser;

import be.seeseemelk.mockbukkit.MockBukkit;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import me.blvckbytes.item_predicate_parser.parse.ParserInput;
import me.blvckbytes.item_predicate_parser.parse.PredicateParserFactory;
import me.blvckbytes.item_predicate_parser.predicate.*;
import me.blvckbytes.item_predicate_parser.token.*;
import me.blvckbytes.item_predicate_parser.translation.*;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.Translatable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

public abstract class ParseTestBase {

  protected static final RecursiveInterceptedEqualityChecker equalityChecker = new RecursiveInterceptedEqualityChecker()
    .expectTypePrefix("me.blvckbytes")
    // Will always be null when creating test-tokens
    .interceptAndReturnTrue(ParserInput.class)
    // Bukkit types are outside the scope of testing
    .interceptAndUseAssertEquals(Enchantment.class)
    .interceptAndUseAssertEquals(PotionEffectType.class)
    .interceptAndUseAssertEquals(Translatable.class)
    .intercept(List.class, (rootActualType, pathParts, expected, actual) -> {
      var lastPathPart = pathParts.getLast();

      // Manual list content comparison
      if (MaterialPredicate.class == lastPathPart.getDeclaringClass()) {
        RecursiveInterceptedEqualityChecker.containsInAnyOrder(actual, expected);
        return true;
      }

      // Do not compare syllable-indices, that has nothing to do with the corresponding test-cases
      return (
        lastPathPart.getName().equals("textIndices") ||
        lastPathPart.getName().equals("partIndices")
      );
    })
    .intercept(Iterable.class, (rootActualType, pathParts, expected, actual) -> {
      var lastPathPart = pathParts.getLast();

      // There's no need to compare the items of a source, as the collision-prefix will always be unique
      return (
        lastPathPart.getDeclaringClass() == TranslatableSource.class &&
        lastPathPart.getName().equals("items")
      );
    });

  private static final Gson gson = new GsonBuilder().create();
  protected static final Logger logger = Logger.getAnonymousLogger();
  protected static TranslationRegistry translationRegistry;
  protected static PredicateParserFactory parserFactory;

  @BeforeAll
  public static void setup() throws IOException {
    MockBukkit.mock();

    try (var inputStream = TranslationRegistry.class.getResourceAsStream("/en_us.json")) {
      if (inputStream == null)
        throw new IllegalStateException("Resource stream was null");

      var languageJson = gson.fromJson(new InputStreamReader(inputStream), JsonObject.class);

      translationRegistry = new TranslationRegistry(languageJson, logger);
      translationRegistry.initialize(makeSources());
    }

    parserFactory = new PredicateParserFactory(translationRegistry);
  }

  @AfterAll
  public static void tearDown() {
    MockBukkit.unmock();
  }

  protected IntegerToken integerToken(int commandArgumentIndex, int firstCharIndex, int lastCharIndex, @Nullable Integer value) {
    return integerToken(commandArgumentIndex, firstCharIndex, lastCharIndex, value, false, ComparisonMode.EQUALS);
  }

  protected IntegerToken integerToken(
    int commandArgumentIndex,
    int firstCharIndex,
    int lastCharIndex,
    @Nullable Integer value,
    boolean wasTimeNotation,
    ComparisonMode comparisonMode
  ) {
    return new IntegerToken(commandArgumentIndex, firstCharIndex, lastCharIndex, null, value, wasTimeNotation, comparisonMode);
  }

  protected ParenthesisToken parenthesisToken(int commandArgumentIndex, int firstCharIndex, boolean isOpening) {
    return new ParenthesisToken(commandArgumentIndex, firstCharIndex, null, isOpening);
  }

  protected QuotedStringToken quotedStringToken(
    int beginCommandArgumentIndex,
    int beginFirstCharIndex,
    int endCommandArgumentIndex,
    int endLastCharIndex,
    String value
  ) {
    return new QuotedStringToken(beginCommandArgumentIndex, beginFirstCharIndex, endCommandArgumentIndex, endLastCharIndex, null, value);
  }

  protected UnquotedStringToken unquotedStringToken(int commandArgumentIndex, int firstCharIndex, String value) {
    return new UnquotedStringToken(commandArgumentIndex, firstCharIndex, null, value);
  }

  protected ItemPredicate andJoin(Token[] keywordTokens, ItemPredicate... predicates) {
    if (predicates.length < 2)
      throw new IllegalStateException("Cannot join less than two predicates");

    var result = predicates[0];

    for (var i = 1; i < predicates.length; ++i) {
      var token = keywordTokens[i - 1];
      result = new ConjunctionNode(token, translationRegistry.lookup(ConjunctionKey.INSTANCE), result, predicates[i]);
    }

    return result;
  }

  protected ItemPredicate orJoin(Token[] keywordTokens, ItemPredicate... predicates) {
    if (predicates.length < 2)
      throw new IllegalStateException("Cannot join less than two predicates");

    var result = predicates[0];

    for (var i = 1; i < predicates.length; ++i)
      result = new DisjunctionNode(keywordTokens[i-1], translationRegistry.lookup(DisjunctionKey.INSTANCE), result, predicates[i]);

    return result;
  }

  protected ItemPredicate negate(UnquotedStringToken keywordToken, ItemPredicate predicate) {
    return new NegationNode(keywordToken, translationRegistry.lookup(NegationKey.INSTANCE), predicate);
  }

  protected ItemPredicate exact(UnquotedStringToken keywordToken, ItemPredicate predicate) {
    return new ExactNode(keywordToken, translationRegistry.lookup(ExactKey.INSTANCE), predicate);
  }

  protected EnchantmentPredicate enchantmentPredicate(Enchantment enchantment, @Nullable IntegerToken level, UnquotedStringToken search) {
    return new EnchantmentPredicate(search, translationRegistry.lookup(enchantment), enchantment, level);
  }

  protected MaterialPredicate materialPredicate(Material material, UnquotedStringToken token) {
    return new MaterialPredicate(token, translationRegistry.lookup(material), List.of(material));
  }

  protected MaterialPredicate materialsPredicate(UnquotedStringToken search, Collection<Material> materials) {
    return new MaterialPredicate(search, null, new ArrayList<>(materials));
  }

  protected PotionEffectPredicate potionEffectPredicate(PotionEffectType type, @Nullable IntegerToken amplifier, @Nullable IntegerToken duration, UnquotedStringToken token) {
    return new PotionEffectPredicate(token, translationRegistry.lookup(type), type, amplifier, duration);
  }

  protected DeteriorationPredicate deteriorationPredicate(@Nullable IntegerToken min, @Nullable IntegerToken max, UnquotedStringToken token) {
    return new DeteriorationPredicate(token, translationRegistry.lookup(DeteriorationKey.INSTANCE), min, max);
  }

  protected AmountPredicate amountPredicate(@Nullable IntegerToken amount, UnquotedStringToken token) {
    return new AmountPredicate(token, translationRegistry.lookup(AmountKey.INSTANCE), amount);
  }

  private static Iterable<TranslatableSource> makeSources() {
    return Arrays.asList(
      new TranslatableSource(Registry.ENCHANTMENT, "[Enchantment] "),
      new TranslatableSource(Registry.EFFECT, "[Effect] "),
      new TranslatableSource(
        Registry.MATERIAL.stream().filter(Material::isItem).toList(),
        "[Material] "
      ),
      new TranslatableSource(List.of(
        DeteriorationKey.INSTANCE,
        NegationKey.INSTANCE,
        DisjunctionKey.INSTANCE,
        ConjunctionKey.INSTANCE,
        ExactKey.INSTANCE,
        AmountKey.INSTANCE
      ), "")
    );
  }
}
