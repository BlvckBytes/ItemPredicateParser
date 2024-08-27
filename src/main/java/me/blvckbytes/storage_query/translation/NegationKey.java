package me.blvckbytes.storage_query.translation;

import org.bukkit.Translatable;
import org.jetbrains.annotations.NotNull;

public class NegationKey implements Translatable {

  public static final NegationKey INSTANCE = new NegationKey();

  private NegationKey() {}

  @Override
  public @NotNull String getTranslationKey() {
    return "custom.storage-query.negation-key";
  }
}
