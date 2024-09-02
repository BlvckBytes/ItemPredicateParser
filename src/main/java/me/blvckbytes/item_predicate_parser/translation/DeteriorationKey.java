package me.blvckbytes.item_predicate_parser.translation;

import org.bukkit.Translatable;
import org.jetbrains.annotations.NotNull;

public class DeteriorationKey implements Translatable {

  public static final DeteriorationKey INSTANCE = new DeteriorationKey();

  private DeteriorationKey() {}

  @Override
  public @NotNull String getTranslationKey() {
    return "custom.item-predicate-parser.deterioration-key";
  }
}
