package me.blvckbytes.storage_query.translation;

import org.bukkit.Translatable;
import org.jetbrains.annotations.NotNull;

public class ExactKey implements Translatable {
  public static final ExactKey INSTANCE = new ExactKey();

  private ExactKey() {}

  @Override
  public @NotNull String getTranslationKey() {
    return "custom.storage-query.exact-key";
  }
}
