package me.blvckbytes.item_predicate_parser.translation;

import org.bukkit.Translatable;
import org.jetbrains.annotations.NotNull;

public class ExactKey implements Translatable {
  public static final ExactKey INSTANCE = new ExactKey();

  private ExactKey() {}

  @Override
  public @NotNull String getTranslationKey() {
    return "custom.item-predicate-parser.exact-key";
  }
}
