package me.blvckbytes.storage_query.translation;

import org.bukkit.Translatable;
import org.jetbrains.annotations.NotNull;

public class DisjunctionKey implements Translatable {

  public static final DisjunctionKey INSTANCE = new DisjunctionKey();

  private DisjunctionKey() {}

  @Override
  public @NotNull String getTranslationKey() {
    return "custom.storage-query.disjunction-key";
  }
}
