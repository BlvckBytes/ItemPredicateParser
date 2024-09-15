package me.blvckbytes.item_predicate_parser.predicate;

import me.blvckbytes.item_predicate_parser.parse.SubstringIndices;
import me.blvckbytes.item_predicate_parser.token.QuotedStringToken;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TextSearchPredicate implements ItemPredicate {

  public final QuotedStringToken token;
  private final List<SubstringIndices> textIndices;

  public TextSearchPredicate(QuotedStringToken token) {
    this.token = token;
    this.textIndices = SubstringIndices.forString(token, token.value(), SubstringIndices.FREE_TEXT_DELIMITER);
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
        token.value(), pendingTextIndices,
        displayName, SubstringIndices.forString(null, displayName, SubstringIndices.FREE_TEXT_DELIMITER)
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
          token.value(), pendingTextIndices,
          loreLine, SubstringIndices.forString(null, loreLine, SubstringIndices.FREE_TEXT_DELIMITER)
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
          token.value(), pendingTextIndices,
          author, SubstringIndices.forString(null, author, SubstringIndices.FREE_TEXT_DELIMITER)
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
          token.value(), pendingTextIndices,
          title, SubstringIndices.forString(null, title, SubstringIndices.FREE_TEXT_DELIMITER)
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
            token.value(), pendingTextIndices,
            page, SubstringIndices.forString(null, page, SubstringIndices.FREE_TEXT_DELIMITER)
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
            token.value(), pendingTextIndices,
            ownerName, SubstringIndices.forString(null, ownerName, SubstringIndices.FREE_TEXT_DELIMITER)
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
    return token.stringify();
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
