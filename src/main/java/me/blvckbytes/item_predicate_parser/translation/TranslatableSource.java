package me.blvckbytes.item_predicate_parser.translation;

import org.bukkit.Translatable;

public record TranslatableSource(
  Iterable<? extends Translatable> items,
  String collisionPrefix
) {
  public TranslatableSource {
    if (!collisionPrefix.isEmpty() && collisionPrefix.charAt(0) == '(')
      throw new IllegalStateException("Please do not use a leading ( on collision-prefixes, as to not introduce parentheses tokenization ambiguities");
  }
}