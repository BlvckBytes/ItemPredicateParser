package me.blvckbytes.item_predicate_parser;

import me.blvckbytes.item_predicate_parser.parse.ItemPredicateParseException;
import me.blvckbytes.item_predicate_parser.parse.ParseConflict;
import me.blvckbytes.item_predicate_parser.parse.Syllables;
import me.blvckbytes.item_predicate_parser.parse.SyllablesMatcher;
import me.blvckbytes.item_predicate_parser.token.Token;
import me.blvckbytes.item_predicate_parser.token.UnquotedStringToken;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class SyllableTests {

  private static final int RANDOM_SYLLABLE_MAX_LENGTH = 128;
  private static final int TOTAL_NUM_RANDOM_SYLLABLES = (int) (Long.SIZE * 4.5);
  private static final String RANDOM_SYLLABLE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789[]'";
  private static final Syllables EMPTY_SYLLABLES = new Syllables(null);
  private static final Token EMPTY_TOKEN = new UnquotedStringToken(0, 0, null, "");

  @Test
  public void testIndicesGeneration() {
    // Leading and trailing delimiter
    makeSyllablesCase(
      "-a-bcd-ef-g-",
      new Syllables(null)
        .add(1, 1, false)
        .add(3, 5, false)
        .add(7, 8, false)
        .add(10, 10, false)
    );

    // Leading delimiter
    makeSyllablesCase(
      "-a-bcd-ef-g",
      new Syllables(null)
        .add(1, 1, false)
        .add(3, 5, false)
        .add(7, 8, false)
        .add(10, 10, false)
    );

    // Trailing delimiter
    makeSyllablesCase(
      "a-bcd-ef-g-",
      new Syllables(null)
        .add(0, 0, false)
        .add(2, 4, false)
        .add(6, 7, false)
        .add(9, 9, false)
    );

    // Trailing delimiter and multiple intermediate delimiters
    makeSyllablesCase(
      "a-bcd----ef---g-",
      new Syllables(null)
        .add(0, 0, false)
        .add(2, 4, false)
        .add(9, 10, false)
        .add(14, 14, false)
    );
  }

  @Test
  public void shouldHandleMultipleTargetsWithoutQueryMatchesResetting() {
    var querySyllables = Syllables.forString(null, "dia-bot-car-ir-gol", Syllables.DELIMITER_SEARCH_PATTERN);
    assertFalse(querySyllables.isWildcardMode());

    var matcher = new SyllablesMatcher();
    matcher.setQuery(querySyllables);

    matcher.setTarget(Syllables.forString(null, "diamond-leggings", Syllables.DELIMITER_SEARCH_PATTERN));
    matcher.match();

    assertUnmatchedSyllablesInAnyOrder(
      matcher, true,
      new Syllables(null)
        .add(4, 6, false)
        .add(8, 10, false)
        .add(12, 13, false)
        .add(15, 17, false)
    );
    assertUnmatchedSyllablesInAnyOrder(
      matcher, false,
      new Syllables(null)
        .add(3, 6, false)
        .add(8, 15, false)
    );

    assertTrue(matcher.hasUnmatchedQuerySyllables());
    assertTrue(matcher.hasUnmatchedTargetSyllables());

    matcher.setTarget(Syllables.forString(null, "iron-golem", Syllables.DELIMITER_SEARCH_PATTERN));
    matcher.match();

    assertUnmatchedSyllablesInAnyOrder(
      matcher, true,
      new Syllables(null)
        .add(4, 6, false)
        .add(8, 10, false)
    );
    assertUnmatchedSyllablesInAnyOrder(
      matcher, false,
      new Syllables(null)
        .add(2, 3, false)
        .add(8, 9, false)
    );

    assertTrue(matcher.hasUnmatchedQuerySyllables());
    assertTrue(matcher.hasUnmatchedTargetSyllables());

    matcher.setTarget(Syllables.forString(null, "bottled-carrot", Syllables.DELIMITER_SEARCH_PATTERN));
    matcher.match();

    assertUnmatchedSyllablesInAnyOrder(matcher, true, EMPTY_SYLLABLES);
    assertUnmatchedSyllablesInAnyOrder(
      matcher, false,
      new Syllables(null)
        .add(3, 6, false)
        .add(11, 13, false)
    );

    assertFalse(matcher.hasUnmatchedQuerySyllables());
    assertTrue(matcher.hasUnmatchedTargetSyllables());
  }

  @Test
  public void shouldHandleMultipleLongTargetsWithoutQueryMatchesResetting() {
    var query = Syllables.forString(null, "hell-diam-gol", Syllables.DELIMITER_SEARCH_PATTERN);
    var sprinkles = new String[] { "hello", "diamond", "gold" };

    assertFalse(query.isWildcardMode());

    var target = Syllables.forString(
      null,
      makeFillerSyllableSequence(new String[] { "hell", "diam", "gol" }, sprinkles),
      Syllables.DELIMITER_SEARCH_PATTERN
    );

    var matcher = new SyllablesMatcher();

    matcher.setQuery(query);
    matcher.setTarget(target);

    matcher.match();
    assertFalse(matcher.hasUnmatchedQuerySyllables());

    var unmatchedTargetSyllables = new ArrayList<Integer>();
    matcher.forEachUnmatchedTargetSyllable((holder, syllable) -> unmatchedTargetSyllables.add(syllable));

    var remainingTargetSyllables = renderSyllables(target, unmatchedTargetSyllables);
    var targetWithoutSprinkles = stripSprinkles(target.container, sprinkles);

    assertEquals(targetWithoutSprinkles + "-o-ond-d", remainingTargetSyllables);
  }

  @Test
  public void shouldHandleMultipleQueries() {
    var targetSyllables = Syllables.forString(null, "one-two-three-four", Syllables.DELIMITER_SEARCH_PATTERN);
    assertFalse(targetSyllables.isWildcardMode());

    var matcher = new SyllablesMatcher();
    matcher.setTarget(targetSyllables);

    matcher.setQuery(Syllables.forString(null, "this-contains-one-and-three", Syllables.DELIMITER_SEARCH_PATTERN));
    matcher.match();

    assertUnmatchedSyllablesInAnyOrder(
      matcher, true,
      new Syllables(null)
        .add(0, 3, false)
        .add(5, 12, false)
        .add(18, 20, false)
    );
    assertUnmatchedSyllablesInAnyOrder(
      matcher, false,
      new Syllables(null)
        .add(4, 6, false)
        .add(14, 17, false)
    );

    assertTrue(matcher.hasUnmatchedQuerySyllables());
    assertTrue(matcher.hasUnmatchedTargetSyllables());

    matcher.setQuery(Syllables.forString(null, "I-hold-syllables-two-and-four", Syllables.DELIMITER_SEARCH_PATTERN));
    matcher.match();

    assertUnmatchedSyllablesInAnyOrder(
      matcher, true,
      new Syllables(null)
        .add(0, 0, false)
        .add(2, 5, false)
        .add(7, 15, false)
        .add(21, 23, false)
    );
    assertUnmatchedSyllablesInAnyOrder(matcher, false, EMPTY_SYLLABLES);

    assertTrue(matcher.hasUnmatchedQuerySyllables());
    assertFalse(matcher.hasUnmatchedTargetSyllables());
  }

  @Test
  public void shouldHandleMultipleTargetsWithQueryMatchesResetting() {
    var querySyllables = Syllables.forString(null, "dia-ax-?", Syllables.DELIMITER_SEARCH_PATTERN);
    assertTrue(querySyllables.isWildcardMode());

    var matcher = new SyllablesMatcher();
    matcher.setQuery(querySyllables);
    matcher.setTarget(Syllables.forString(null, "diamond-axe", Syllables.DELIMITER_SEARCH_PATTERN));

    matcher.match();

    assertUnmatchedSyllablesInAnyOrder(matcher, true, EMPTY_SYLLABLES);
    assertUnmatchedSyllablesInAnyOrder(
      matcher, false,
      new Syllables(null)
        .add(3, 6, false)
        .add(10, 10, false)
    );

    assertFalse(matcher.hasUnmatchedQuerySyllables());
    assertTrue(matcher.hasUnmatchedTargetSyllables());

    matcher.resetQueryMatches();
    matcher.setTarget(Syllables.forString(null, "diamond-pickaxe", Syllables.DELIMITER_SEARCH_PATTERN));

    matcher.match();

    assertUnmatchedSyllablesInAnyOrder(matcher, true, EMPTY_SYLLABLES);
    assertUnmatchedSyllablesInAnyOrder(
      matcher, false,
      new Syllables(null)
        .add(3, 6, false)
        .add(8, 11, false)
        .add(14, 14, false)
    );

    assertFalse(matcher.hasUnmatchedQuerySyllables());
    assertTrue(matcher.hasUnmatchedTargetSyllables());

    matcher.resetQueryMatches();
    matcher.setTarget(Syllables.forString(null, "diamond-chestplate", Syllables.DELIMITER_SEARCH_PATTERN));

    matcher.match();

    assertUnmatchedSyllablesInAnyOrder(
      matcher, true,
      new Syllables(null)
        .add(4, 5, false)
    );
    assertUnmatchedSyllablesInAnyOrder(
      matcher, false,
      new Syllables(null)
        .add(3, 6, false)
        .add(8, 17, false)
    );

    assertTrue(matcher.hasUnmatchedQuerySyllables());
    assertTrue(matcher.hasUnmatchedTargetSyllables());
  }

  @Test
  public void shouldHandlePositiveMatches() {
    makeUnmatchedCase(
      "HELLO,-WORLD", "he-orld", false,
      new Syllables(null)
        .add(2, 5, false)
        .add(7, 7, false),
      EMPTY_SYLLABLES
    );

    makeUnmatchedCase(
      "Diamantbrustplatte", "dia-brus", false,
      new Syllables(null)
        .add(3, 6, false)
        .add(11, 17, false),
      EMPTY_SYLLABLES
    );

    makeUnmatchedCase(
      "Diamantbrustplatte", "gold-brus", false,
      new Syllables(null)
        .add(0, 6, false)
        .add(11, 17, false),
      new Syllables(null)
        .add(0, 3, false)
    );
  }

  @Test
  public void shouldHandleNegativeMatches() {
    makeUnmatchedCase(
      "Diamantbrustplatte", "!dia-brus", false,
      new Syllables(null)
        .add(3, 6, false)
        .add(11, 17, false),
      new Syllables(null)
        .add(1, 3, true)
    );

    makeUnmatchedCase(
      "Red-Wool", "!re-wo", false,
      new Syllables(null)
        .add(2, 2, false)
        .add(6, 7, false),
      new Syllables(null)
        .add(1, 2, true)
    );
  }

  @Test
  public void shouldHandleMatchingOnReminders() {
    makeUnmatchedCase(
      "Diamantbrustplatte", "dia-bru-man-pla", false,
      new Syllables(null)
        .add(6, 6, false)
        .add(10, 11, false)
        .add(15, 17, false),
      EMPTY_SYLLABLES
    );
  }

  @Test
  public void shouldHandleWildcardMatches() {
    makeUnmatchedCase(
      "Oak-Sign", "sign-?", true,
      new Syllables(null)
        .add(0, 2, false),
      EMPTY_SYLLABLES
    );

    assertEquals(
      ParseConflict.ONLY_SEARCH_PATTERN_WILDCARD,
      assertThrows(
        ItemPredicateParseException.class,
        () -> Syllables.forString(EMPTY_TOKEN, "?", Syllables.DELIMITER_SEARCH_PATTERN)
      ).getConflict()
    );

    assertEquals(
      ParseConflict.MULTIPLE_SEARCH_PATTERN_WILDCARDS,
      assertThrows(
        ItemPredicateParseException.class,
        () -> Syllables.forString(EMPTY_TOKEN, "?-?", Syllables.DELIMITER_SEARCH_PATTERN)
      ).getConflict()
    );
  }

  @Test
  public void shouldIgnoreStandardColorSequences() {
    // debug-info: 71 chars total, 42 color-sequence chars, 29 other chars
    //                                  v-15            v-31                    v-55       v-66
    var coloredString = "§aH§be§cl§dl§eo-§fW§0o§1r§2l§3d-§4T§5e§6s§7t§8i§9n§m§ag-§ncolo§krs-§r:)";

    makeUnmatchedCase(
      coloredString, "hello-world-testing-colors-:)", false,
      EMPTY_SYLLABLES,
      EMPTY_SYLLABLES
    );

    makeUnmatchedCase(
      coloredString, "testing", false,
      new Syllables(null)
        .add(0, 14, false)
        .add(16, 30, false)
        .add(56, 65, false)
        .add(67, 70, false),
      EMPTY_SYLLABLES
    );
  }

  @Test
  public void shouldResetMatches() {
    var remainingTargetSyllables = new Syllables(null)
      .add(2, 5, false)
      .add(8, 10, false);

    var matcher = makeUnmatchedCase(
      "abcdefghijk", "ab-gh", false,
      remainingTargetSyllables,
      EMPTY_SYLLABLES
    );

    matcher.resetQueryMatches();

    var querySyllables = new Syllables(null)
      .add(0, 1, false)
      .add(3, 4, false);

    assertUnmatchedSyllablesInAnyOrder(matcher, true, querySyllables);
    assertUnmatchedSyllablesInAnyOrder(matcher, false, remainingTargetSyllables);

    matcher.resetTargetMatches();

    assertUnmatchedSyllablesInAnyOrder(matcher, true, querySyllables);
    assertUnmatchedSyllablesInAnyOrder(
      matcher, false,
      new Syllables(null)
        .add(0, 10, false)
    );
  }

  private String stripSprinkles(String input, String[] sprinkles) {
    var result = input;

    for (var sprinkle : sprinkles) {
      var sprinkleIndex = result.indexOf(sprinkle);
      var sprinkleEnd = sprinkleIndex + sprinkle.length() - 1;

      if (sprinkleIndex == 0)
        result = result.substring(sprinkleEnd + 2);

      else if (sprinkleEnd == result.length() - 1)
        result = result.substring(0, sprinkleIndex - 1);

      else {
        result = (
          result.substring(0, sprinkleIndex) +
            result.substring(sprinkleEnd + 2)
        );
      }
    }
    return result;
  }

  private Set<Integer> generateUniqueRandomPositiveNumbers(int count) {
    var result = new HashSet<Integer>();

    while (result.size() != count)
      result.add(ThreadLocalRandom.current().nextInt(0, TOTAL_NUM_RANDOM_SYLLABLES));

    return result;
  }

  private String generateRandomSyllable() {
    var result = new StringBuilder();
    var syllableLength = ThreadLocalRandom.current().nextInt(1, RANDOM_SYLLABLE_MAX_LENGTH);

    for (var i = 0; i < syllableLength; ++i) {
      result.append(RANDOM_SYLLABLE_CHARS.charAt(
        ThreadLocalRandom.current().nextInt(0, RANDOM_SYLLABLE_CHARS.length())
      ));
    }

    return result.toString();
  }

  private String makeFillerSyllableSequence(String[] illegalSequences, String[] sprinkles) {
    var result = new StringBuilder();
    var sprinkleIndices = generateUniqueRandomPositiveNumbers(sprinkles.length);
    var nextSprinkleIndex = 0;

    for (var i = 0; i < TOTAL_NUM_RANDOM_SYLLABLES; ++i) {
      String currentSyllableContent;

      if (sprinkleIndices.remove(i))
        currentSyllableContent = sprinkles[nextSprinkleIndex++];
      else {
        generator: while (true) {
          currentSyllableContent = generateRandomSyllable();

          for (var illegalSequence : illegalSequences) {
            if (currentSyllableContent.toLowerCase().contains(illegalSequence.toLowerCase()))
              continue generator;;
          }

          break;
        }
      }

      if (i != 0)
        result.append('-');

      result.append(currentSyllableContent);
    }

    return result.toString();
  }

  private SyllablesMatcher makeUnmatchedCase(
    String target, String query, boolean queryContainsWildcard,
    Syllables expectedUnmatchedTargetSyllables,
    Syllables expectedUnmatchedQuerySyllables
  ) {
    var matcher = new SyllablesMatcher();

    var querySyllables = Syllables.forString(null, query, Syllables.DELIMITER_SEARCH_PATTERN);

    matcher.setTarget(Syllables.forString(null, target, Syllables.DELIMITER_SEARCH_PATTERN));
    matcher.setQuery(querySyllables);

    assertEquals(queryContainsWildcard, querySyllables.isWildcardMode());

    matcher.match();

    assertUnmatchedSyllablesInAnyOrder(matcher, true, expectedUnmatchedQuerySyllables);
    assertUnmatchedSyllablesInAnyOrder(matcher, false, expectedUnmatchedTargetSyllables);

    return matcher;
  }

  private void assertUnmatchedSyllablesInAnyOrder(SyllablesMatcher matcher, boolean query, Syllables expected) {
    List<Integer> expectedSyllables = new ArrayList<>();

    for (var expectedSyllableIndex = 0; expectedSyllableIndex < expected.size(); ++expectedSyllableIndex)
      expectedSyllables.add(expected.getSyllable(expectedSyllableIndex));

    List<Integer> actualSyllables = new ArrayList<>();

    if (query)
      matcher.forEachUnmatchedQuerySyllable((holder, syllable) -> actualSyllables.add(syllable));
    else
      matcher.forEachUnmatchedTargetSyllable((holder, syllable) -> actualSyllables.add(syllable));

    try {
      assertThat(expectedSyllables, Matchers.containsInAnyOrder(actualSyllables.toArray()));
    } catch (AssertionError e) {
      var holder = query ? matcher.getQuery() : matcher.getTarget();

      var renderedExpectedSyllables = renderSyllables(holder, expectedSyllables);
      var renderedActualSyllables = renderSyllables(holder, actualSyllables);

      throw new AssertionError("Expected \"" + renderedExpectedSyllables + "\" but got \"" + renderedActualSyllables + "\"; " + e.getMessage());
    }
  }

  private String renderSyllables(Syllables holder, List<Integer> syllables) {
    var result = new StringBuilder();

    for (var syllable : syllables) {
      if (!result.isEmpty())
        result.append('-');

      result.append(
        holder.container
          .substring(Syllables.getStartIndex(syllable), Syllables.getEndIndex(syllable) + 1)
          .replace("-", "\\-") // Signal substring hyphens (contrast with delimiters)
      );
    }

    return result.toString();
  }

  private void makeSyllablesCase(String input, Syllables expectedSyllables) {
    var actualSyllables = Syllables.forString(null, input, Syllables.DELIMITER_SEARCH_PATTERN);

    assertEquals(expectedSyllables.size(), actualSyllables.size(), "Expected syllable-count to equal");

    for (var syllableIndex = 0; syllableIndex < expectedSyllables.size(); ++syllableIndex) {
      var expectedSyllable = expectedSyllables.getSyllable(syllableIndex);
      var actualSyllable = actualSyllables.getSyllable(syllableIndex);

      assertEquals(Syllables.getStartIndex(expectedSyllable), Syllables.getStartIndex(actualSyllable), "Expected start-indices to equal at index " + syllableIndex);
      assertEquals(Syllables.getEndIndex(expectedSyllable), Syllables.getEndIndex(actualSyllable), "Expected end-indices to equal at index " + syllableIndex);
      assertEquals(Syllables.isNegated(expectedSyllable), Syllables.isNegated(actualSyllable), "Expected isNegated-flags to equal at index " + syllableIndex);
    }
  }
}
