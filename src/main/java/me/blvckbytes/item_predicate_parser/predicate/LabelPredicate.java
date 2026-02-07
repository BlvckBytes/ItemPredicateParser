package me.blvckbytes.item_predicate_parser.predicate;

import me.blvckbytes.item_predicate_parser.predicate.stringify.StringifyHandler;
import me.blvckbytes.item_predicate_parser.token.UnquotedStringToken;
import me.blvckbytes.syllables_matcher.Syllables;
import me.blvckbytes.syllables_matcher.SyllablesMatcher;
import me.blvckbytes.syllables_matcher.WildcardMode;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Objects;

public class LabelPredicate implements ItemPredicate {

  public final UnquotedStringToken token;
  private final Syllables tokenSyllables;

  public LabelPredicate(UnquotedStringToken token) {
    this.token = token;
    this.tokenSyllables = Syllables.forString(token.value(), Syllables.DELIMITER_SEARCH_PATTERN);
  }

  @Override
  public @Nullable ItemPredicate testForFailure(PredicateState state) {
    // Labels carry a purely semantic meaning - they're essentially a NOOP.
    return null;
  }

  @Override
  public void stringify(StringifyHandler handler) {
    handler.stringify(this, output -> output.appendString(token.stringify()));
  }

  @Override
  public boolean containsOrEqualsPredicate(ItemPredicate node, EnumSet<ComparisonFlag> comparisonFlags) {
    return _equals(node, comparisonFlags);
  }

  @Override
  public boolean equals(Object other) {
    return _equals(other, EnumSet.noneOf(ComparisonFlag.class));
  }

  private boolean _equals(Object other, EnumSet<ComparisonFlag> comparisonFlags) {
    if (!(other instanceof LabelPredicate otherPredicate))
      return false;

    // Labels are, by definition, case-insensitive - for convenience.
    if (token.value().equalsIgnoreCase(otherPredicate.token.value()))
      return true;

    if (!comparisonFlags.contains(ComparisonFlag.LABEL_PREDICATE__USE_MATCHER))
      return false;

    var matcher = new SyllablesMatcher();

    matcher.setQuery(otherPredicate.tokenSyllables);
    matcher.setTarget(this.tokenSyllables);
    matcher.match();

    var wildcardMode = otherPredicate.tokenSyllables.getWildcardMode();

    if (wildcardMode != WildcardMode.NONE) {
      if (wildcardMode == WildcardMode.EXCLUDING_EXACT_MATCH && !matcher.hasUnmatchedTargetSyllables())
        return false;
    }

    return !matcher.hasUnmatchedQuerySyllables();
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(token.value());
  }
}
