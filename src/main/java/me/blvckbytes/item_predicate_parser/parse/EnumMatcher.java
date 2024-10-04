package me.blvckbytes.item_predicate_parser.parse;

import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class EnumMatcher<T extends Enum<?>> {

  private final NormalizedConstant<T>[] constants;
  public final List<String> stringValues;

  @SuppressWarnings("unchecked")
  public EnumMatcher(T[] values) {
    this.constants = new NormalizedConstant[values.length];

    for (var i = 0; i < values.length; ++i)
      this.constants[i] = new NormalizedConstant<>(values[i]);

    // Sort just like the client would, so that the first match is equal to
    // the first entry in the suggestion-list displayed to the user
    Arrays.sort(this.constants, Comparator.comparing(a -> a.normalizedName));

    var normalizedConstantNames = new ArrayList<String>();

    for (var constant : constants)
      normalizedConstantNames.add(constant.normalizedName);

    stringValues = Collections.unmodifiableList(normalizedConstantNames);
  }

  public List<String> createCompletions(String input) {
    var result = new ArrayList<String>();

    forEachMatch(input, match -> result.add(match.normalizedName));

    return result;
  }

  public @Nullable NormalizedConstant<T> matchFirst(String input) {
    return forEachMatch(input, match -> false);
  }

  private @Nullable NormalizedConstant<T> forEachMatch(String input, Function<NormalizedConstant<T>, Boolean> matchHandler) {
    var inputIndices = SubstringIndices.forString(null, input, '-');

    for (var translationLanguage : constants) {
      var pendingInputSubstrings = new ArrayList<>(inputIndices);

      SubstringIndices.matchQuerySubstrings(
        input, pendingInputSubstrings,
        translationLanguage.normalizedName, translationLanguage.getNormalizedNameIndices()
      );

      if (pendingInputSubstrings.isEmpty()) {
        if (!matchHandler.apply(translationLanguage))
          return translationLanguage;
      }
    }

    return null;
  }
}
