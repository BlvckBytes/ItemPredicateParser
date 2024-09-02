package me.blvckbytes.item_predicate_parser.translation;

import org.bukkit.Translatable;
import org.jetbrains.annotations.NotNull;

public class DisjunctionKey implements Translatable {

  public static final DisjunctionKey INSTANCE = new DisjunctionKey();

  private DisjunctionKey() {}

  @Override
  public @NotNull String getTranslationKey() {
    return "custom.item-predicate-parser.disjunction-key";
  }
}
