package me.blvckbytes.item_predicate_parser.translation;

import me.blvckbytes.item_predicate_parser.translation.keyed.LangKeyed;

public class ConjunctionKey implements LangKeyed<ConjunctionKey> {

  public static final ConjunctionKey INSTANCE = new ConjunctionKey();

  private ConjunctionKey() {}

  @Override
  public String getLanguageFileKey() {
    return "custom.item-predicate-parser.conjunction-key";
  }

  @Override
  public ConjunctionKey getWrapped() {
    return this;
  }
}
