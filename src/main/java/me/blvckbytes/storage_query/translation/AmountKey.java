package me.blvckbytes.storage_query.translation;

import org.bukkit.Translatable;
import org.jetbrains.annotations.NotNull;

public class AmountKey implements Translatable {
  public static final AmountKey INSTANCE = new AmountKey();

  private AmountKey() {}

  @Override
  public @NotNull String getTranslationKey() {
    return "custom.storage-query.amount-key";
  }
}
