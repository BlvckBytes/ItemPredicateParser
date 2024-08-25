package me.blvckbytes.storage_query.translation;

import org.bukkit.Translatable;

public record TranslatableSource(
  Iterable<? extends Translatable> items,
  String collisionPrefix
) {}