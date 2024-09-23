package me.blvckbytes.item_predicate_parser.translation;

import me.blvckbytes.item_predicate_parser.translation.keyed.LangKeyed;

public class ExactKey implements LangKeyed<ExactKey> {
  public static final ExactKey INSTANCE = new ExactKey();

  private ExactKey() {}

  @Override
  public String getLanguageFileKey() {
    return "custom.item-predicate-parser.exact-key";
 }

  @Override
  public ExactKey getWrapped() {
    return this;
  }
}
