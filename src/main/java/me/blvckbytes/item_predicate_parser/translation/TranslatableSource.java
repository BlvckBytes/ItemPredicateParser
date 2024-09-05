package me.blvckbytes.item_predicate_parser.translation;

import org.bukkit.Translatable;

public record TranslatableSource(
  Iterable<? extends Translatable> items,
  String collisionPrefix
) {}