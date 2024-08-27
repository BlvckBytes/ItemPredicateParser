package me.blvckbytes.storage_query;

import me.blvckbytes.storage_query.parse.SearchWildcardPresence;
import me.blvckbytes.storage_query.parse.SubstringIndices;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class SubstringIndicesTests {

  @Test
  public void testIndicesGeneration() {
    // Leading and trailing delimiter
    makeIndicesGenCase("-a-bcd-ef-g-", Arrays.asList(
      new SubstringIndices(1, 1),
      new SubstringIndices(3, 5),
      new SubstringIndices(7, 8),
      new SubstringIndices(10, 10)
    ));

    // Leading delimiter
    makeIndicesGenCase("-a-bcd-ef-g", Arrays.asList(
      new SubstringIndices(1, 1),
      new SubstringIndices(3, 5),
      new SubstringIndices(7, 8),
      new SubstringIndices(10, 10)
    ));

    // Trailing delimiter
    makeIndicesGenCase("a-bcd-ef-g-", Arrays.asList(
      new SubstringIndices(0, 0),
      new SubstringIndices(2, 4),
      new SubstringIndices(6, 7),
      new SubstringIndices(9, 9)
    ));

    // Trailing delimiter and multiple intermediate delimiters
    makeIndicesGenCase("a-bcd----ef---g-", Arrays.asList(
      new SubstringIndices(0, 0),
      new SubstringIndices(2, 4),
      new SubstringIndices(9, 10),
      new SubstringIndices(14, 14)
    ));
  }

  @Test
  public void testListModifications() {
    makeListModCase("HELLO,-WORLD", "he-orld", "LLO, W", "");
    makeListModCase("Diamondchestplate", "dia-chest", "mond plate", "");
    makeListModCase("Diamondchestplate", "gold-chest", "Diamond plate", "gold");
  }

  @Test
  public void shouldHandleNegationsCorrectly() {
    makeListModCase("Diamondchestplate", "!dia-chest", "mond plate", "dia");
    makeListModCase("Red-Wool", "!re-wo", "d ol", "re");
  }

  @Test
  public void shouldDetectSearchWildcards() {
    assertEquals(
      SearchWildcardPresence.PRESENT,
      makeListModCase("oak-sign", "sign-?", "oak", "")
    );

    assertEquals(
      SearchWildcardPresence.ABSENT,
      makeListModCase("oak-sign", "sign-oa", "k", "")
    );

    assertEquals(
      SearchWildcardPresence.CONFLICT_OCCURRED_REPEATEDLY,
      makeListModCase("oak-sign", "sign-?-o-?", "ak", null)
    );
  }

  private void makeIndicesGenCase(String input, List<SubstringIndices> expectedIndicesList) {
    var indicesList = SubstringIndices.forString(input, SubstringIndices.SEARCH_PATTERN_DELIMITERS);

    for (var i = 0; i < expectedIndicesList.size(); ++i) {
      if (i >= indicesList.size())
        fail("Missing indices at index " + i);

      var currentIndices = indicesList.get(i);
      var expectedIndices = expectedIndicesList.get(i);

      assertEquals(expectedIndices.start(), currentIndices.start());
      assertEquals(expectedIndices.end(), currentIndices.end());
    }
  }

  private SearchWildcardPresence makeListModCase(
    String text,
    String query,
    String expectedRemainingText,
    @Nullable String expectedPendingQuery
  ) {
    var textIndices = SubstringIndices.forString(text, SubstringIndices.SEARCH_PATTERN_DELIMITERS);
    var queryIndices = SubstringIndices.forString(query, SubstringIndices.SEARCH_PATTERN_DELIMITERS);

    var pendingQueryIndices = new ArrayList<>(queryIndices);
    var remainingTextIndices = new ArrayList<>(textIndices);

    var presence = SubstringIndices.matchQuerySubstrings(query, pendingQueryIndices, text, remainingTextIndices);

    if (expectedPendingQuery != null)
      assertEquals(expectedPendingQuery, joinIndices(query, pendingQueryIndices));

    assertEquals(expectedRemainingText, joinIndices(text, remainingTextIndices));

    return presence;
  }

  private String joinIndices(String text, Collection<SubstringIndices> indices) {
    return indices.stream().map(x -> text.substring(x.start(), x.end() + 1)).collect(Collectors.joining(" "));
  }
}
