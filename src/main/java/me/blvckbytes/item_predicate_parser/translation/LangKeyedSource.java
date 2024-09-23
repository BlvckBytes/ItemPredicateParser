package me.blvckbytes.item_predicate_parser.translation;

import me.blvckbytes.item_predicate_parser.translation.keyed.LangKeyed;

public record LangKeyedSource(
  Iterable<? extends LangKeyed<?>> items,
  String collisionPrefix
) {
  public LangKeyedSource(
    Iterable<? extends LangKeyed<?>> items,
    String collisionPrefix
  ) {
    this.items = items;
    this.collisionPrefix = TranslatedLangKeyed.normalize(collisionPrefix.trim()) + "-";
  }
}