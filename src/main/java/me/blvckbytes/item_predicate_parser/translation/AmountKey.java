package me.blvckbytes.item_predicate_parser.translation;

import me.blvckbytes.item_predicate_parser.translation.keyed.LangKeyed;

public class AmountKey implements LangKeyed<AmountKey> {
  public static final AmountKey INSTANCE = new AmountKey();

  private AmountKey() {}

  @Override
  public String getLanguageFileKey() {
    return "custom.item-predicate-parser.amount-key";
  }

  @Override
  public AmountKey getWrapped() {
    return this;
  }
}
