package me.blvckbytes.item_predicate_parser.event;

import me.blvckbytes.item_predicate_parser.predicate.ItemPredicate;
import me.blvckbytes.item_predicate_parser.predicate.stringify.PlainStringifier;
import me.blvckbytes.item_predicate_parser.translation.TranslationLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PredicateAndLanguage {

  public final @NotNull ItemPredicate predicate;
  public final @NotNull TranslationLanguage language;

  private @Nullable String tokenPredicateString;
  private @Nullable String expandedPredicateString;

  public PredicateAndLanguage(@NotNull ItemPredicate predicate, @NotNull TranslationLanguage language) {
    this.predicate = predicate;
    this.language = language;
  }

  public @NotNull String getTokenPredicateString() {
    if (tokenPredicateString == null)
      tokenPredicateString = PlainStringifier.stringify(predicate, true);

    return tokenPredicateString;
  }

  public @NotNull String getExpandedPredicateString() {
    if (expandedPredicateString == null)
      expandedPredicateString = PlainStringifier.stringify(predicate, false);

    return expandedPredicateString;
  }

  public String getLanguageNormalizedName() {
    return TranslationLanguage.matcher.getNormalizedName(language);
  }
}
