package me.blvckbytes.item_predicate_parser.translation;

import me.blvckbytes.item_predicate_parser.translation.keyed.LangKeyed;

public class DisjunctionKey implements LangKeyed<DisjunctionKey> {

  public static final DisjunctionKey INSTANCE = new DisjunctionKey();

  private DisjunctionKey() {}

  @Override
  public String getLanguageFileKey() {
    return "custom.item-predicate-parser.disjunction-key";
  }

  @Override
  public DisjunctionKey getWrapped() {
    return this;
  }
}
