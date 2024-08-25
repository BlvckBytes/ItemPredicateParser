package me.blvckbytes.storage_query.predicate;

import me.blvckbytes.storage_query.parse.SubstringIndices;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TextSearchPredicate implements ItemPredicate {

  public final String text;
  private final List<SubstringIndices> textIndices;

  public TextSearchPredicate(String text) {
    this.text = text;
    this.textIndices = SubstringIndices.forString(text, SubstringIndices.FREE_TEXT_DELIMITERS);
  }

  @Override
  public boolean test(ItemStack item) {
    var meta = item.getItemMeta();

    if (meta == null)
      return false;

    var pendingTextIndices = new ArrayList<>(textIndices);

    // ================================================================================
    // Display Name
    // ================================================================================

    if (meta.hasDisplayName()) {
      var displayName = meta.getDisplayName();

      SubstringIndices.matchQuerySubstrings(
        text, pendingTextIndices,
        displayName, SubstringIndices.forString(meta.getDisplayName(), SubstringIndices.FREE_TEXT_DELIMITERS)
      );

      if (pendingTextIndices.isEmpty())
        return true;
    }

    // ================================================================================
    // Lore Lines
    // ================================================================================

    if (meta.hasLore()) {
      for (var loreLine : Objects.requireNonNull(meta.getLore())) {
        SubstringIndices.matchQuerySubstrings(
          text, pendingTextIndices,
          loreLine, SubstringIndices.forString(loreLine, SubstringIndices.FREE_TEXT_DELIMITERS)
        );

        if (pendingTextIndices.isEmpty())
          return true;
      }
    }

    if (meta instanceof BookMeta bookMeta) {

      // ================================================================================
      // Book Author
      // ================================================================================

      if (bookMeta.hasAuthor()) {
        var author = Objects.requireNonNull(bookMeta.getAuthor());

        SubstringIndices.matchQuerySubstrings(
          text, pendingTextIndices,
          author, SubstringIndices.forString(author, SubstringIndices.FREE_TEXT_DELIMITERS)
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
          title, SubstringIndices.forString(title, SubstringIndices.FREE_TEXT_DELIMITERS)
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
            page, SubstringIndices.forString(page, SubstringIndices.FREE_TEXT_DELIMITERS)
          );

          if (pendingTextIndices.isEmpty())
            return true;
        }
      }
    }

    // ================================================================================
    // Skull Owner
    // ================================================================================

    if (meta instanceof SkullMeta skullMeta) {
      var ownerProfile = skullMeta.getOwnerProfile();

      if (ownerProfile != null) {
        var ownerName = ownerProfile.getName();

        if (ownerName != null) {
          SubstringIndices.matchQuerySubstrings(
            text, pendingTextIndices,
            ownerName, SubstringIndices.forString(ownerName, SubstringIndices.FREE_TEXT_DELIMITERS)
          );

          if (pendingTextIndices.isEmpty())
            return true;
        }
      }
    }

    return pendingTextIndices.isEmpty();
  }

  @Override
  public String stringify() {
    return "\"" + text + "\"";
  }
}
