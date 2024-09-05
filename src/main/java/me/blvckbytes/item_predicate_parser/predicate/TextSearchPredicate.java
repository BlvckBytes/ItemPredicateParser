package me.blvckbytes.item_predicate_parser.predicate;

import me.blvckbytes.item_predicate_parser.parse.SubstringIndices;
import me.blvckbytes.item_predicate_parser.token.QuotedStringToken;
import me.blvckbytes.item_predicate_parser.token.UnquotedStringToken;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TextSearchPredicate implements ItemPredicate {

  public final String text;
  private final List<SubstringIndices> textIndices;

  public TextSearchPredicate(QuotedStringToken token) {
    this.text = token.value();
    this.textIndices = SubstringIndices.forString(token.commandArgumentIndex(), token.value(), SubstringIndices.FREE_TEXT_DELIMITERS);
  }

  @Override
  public boolean test(PredicateState state) {
    if (state.meta == null)
      return false;

    var pendingTextIndices = new ArrayList<>(textIndices);

    // ================================================================================
    // Display Name
    // ================================================================================

    if (state.meta.hasDisplayName()) {
      var displayName = state.meta.getDisplayName();

      SubstringIndices.matchQuerySubstrings(
        text, pendingTextIndices,
        displayName, SubstringIndices.forString(null, displayName, SubstringIndices.FREE_TEXT_DELIMITERS)
      );

      if (pendingTextIndices.isEmpty())
        return true;
    }

    // ================================================================================
    // Lore Lines
    // ================================================================================

    if (state.meta.hasLore()) {
      for (var loreLine : Objects.requireNonNull(state.meta.getLore())) {
        SubstringIndices.matchQuerySubstrings(
          text, pendingTextIndices,
          loreLine, SubstringIndices.forString(null, loreLine, SubstringIndices.FREE_TEXT_DELIMITERS)
        );

        if (pendingTextIndices.isEmpty())
          return true;
      }
    }

    if (state.meta instanceof BookMeta bookMeta) {

      // ================================================================================
      // Book Author
      // ================================================================================

      if (bookMeta.hasAuthor()) {
        var author = Objects.requireNonNull(bookMeta.getAuthor());

        SubstringIndices.matchQuerySubstrings(
          text, pendingTextIndices,
          author, SubstringIndices.forString(null, author, SubstringIndices.FREE_TEXT_DELIMITERS)
        );

        if (pendingTextIndices.isEmpty())
          return true;
      }

      // ================================================================================
      // Book Title
      // ================================================================================

      if (bookMeta.hasTitle()) {
        var title = Objects.requireNonNull(bookMeta.getTitle());

        SubstringIndices.matchQuerySubstrings(
          text, pendingTextIndices,
          title, SubstringIndices.forString(null, title, SubstringIndices.FREE_TEXT_DELIMITERS)
        );

        if (pendingTextIndices.isEmpty())
          return true;
      }

      // ================================================================================
      // Book Pages
      // ================================================================================

      if (bookMeta.hasPages()) {
        for (var page : bookMeta.getPages()) {
          SubstringIndices.matchQuerySubstrings(
            text, pendingTextIndices,
            page, SubstringIndices.forString(null, page, SubstringIndices.FREE_TEXT_DELIMITERS)
          );

          if (pendingTextIndices.isEmpty())
            return true;
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
          SubstringIndices.matchQuerySubstrings(
            text, pendingTextIndices,
            ownerName, SubstringIndices.forString(null, ownerName, SubstringIndices.FREE_TEXT_DELIMITERS)
          );

          if (pendingTextIndices.isEmpty())
            return true;
        }
      }
    }

    return pendingTextIndices.isEmpty();
  }

  @Override
  public String stringify(boolean useTokens) {
    return "\"" + UnquotedStringToken.escapeDoubleQuotes(text) + "\"";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TextSearchPredicate that)) return false;
    return Objects.equals(text, that.text);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(text);
  }
}
