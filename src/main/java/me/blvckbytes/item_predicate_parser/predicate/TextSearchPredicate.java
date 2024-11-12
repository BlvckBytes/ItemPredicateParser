package me.blvckbytes.item_predicate_parser.predicate;

import me.blvckbytes.item_predicate_parser.token.QuotedStringToken;
import me.blvckbytes.syllables_matcher.Syllables;
import me.blvckbytes.syllables_matcher.SyllablesMatcher;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class TextSearchPredicate implements ItemPredicate {

  public final QuotedStringToken token;
  private final Syllables tokenSyllables;

  public TextSearchPredicate(QuotedStringToken token) {
    this.token = token;
    this.tokenSyllables = Syllables.forString(token.value(), Syllables.DELIMITER_FREE_TEXT);
  }

  @Override
  public @Nullable ItemPredicate testForFailure(PredicateState state) {
    if (state.meta == null)
      return this;

    var matcher = new SyllablesMatcher();

    matcher.setQuery(tokenSyllables);

    // ================================================================================
    // Display Name
    // ================================================================================

    if (state.meta.hasDisplayName()) {
      var displayNameSyllables = Syllables.forString(state.meta.getDisplayName(), Syllables.DELIMITER_FREE_TEXT);

      matcher.setTarget(displayNameSyllables);
      matcher.match();

      if (!matcher.hasUnmatchedQuerySyllables())
        return null;
    }

    // ================================================================================
    // Lore Lines
    // ================================================================================

    if (state.meta.hasLore()) {
      for (var loreLine : Objects.requireNonNull(state.meta.getLore())) {
        var loreLineSyllables = Syllables.forString(loreLine, Syllables.DELIMITER_FREE_TEXT);

        matcher.setTarget(loreLineSyllables);
        matcher.match();

        if (!matcher.hasUnmatchedQuerySyllables())
          return null;
      }
    }

    if (state.meta instanceof BookMeta bookMeta) {

      // ================================================================================
      // Book Author
      // ================================================================================

      if (bookMeta.hasAuthor()) {
        var authorSyllables = Syllables.forString(Objects.requireNonNull(bookMeta.getAuthor()), Syllables.DELIMITER_FREE_TEXT);

        matcher.setTarget(authorSyllables);
        matcher.match();

        if (!matcher.hasUnmatchedQuerySyllables())
          return null;
      }

      // ================================================================================
      // Book Title
      // ================================================================================

      if (bookMeta.hasTitle()) {
        var titleSyllables = Syllables.forString(Objects.requireNonNull(bookMeta.getTitle()), Syllables.DELIMITER_FREE_TEXT);

        matcher.setTarget(titleSyllables);
        matcher.match();

        if (!matcher.hasUnmatchedQuerySyllables())
          return null;
      }

      // ================================================================================
      // Book Pages
      // ================================================================================

      if (bookMeta.hasPages()) {
        for (var page : bookMeta.getPages()) {
          var pageSyllables = Syllables.forString(page, Syllables.DELIMITER_FREE_TEXT);

          matcher.setTarget(pageSyllables);
          matcher.match();

          if (!matcher.hasUnmatchedQuerySyllables())
            return null;
        }
      }
    }

    // ================================================================================
    // Skull Owner
    // ================================================================================

    if (state.meta instanceof SkullMeta skullMeta) {
      var ownerProfile = skullMeta.getOwnerProfile();

      if (ownerProfile != null) {
        var ownerName = ownerProfile.getName();

        if (ownerName != null) {
          var ownerNameSyllables = Syllables.forString(ownerName, Syllables.DELIMITER_FREE_TEXT);

          matcher.setTarget(ownerNameSyllables);
          matcher.match();

          if (!matcher.hasUnmatchedQuerySyllables())
            return null;
        }
      }
    }

    return this;
  }

  @Override
  public void stringify(StringifyState state) {
    state.appendString(token.stringify());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TextSearchPredicate that)) return false;
    return Objects.equals(token.value(), that.token.value());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(token.value());
  }
}
