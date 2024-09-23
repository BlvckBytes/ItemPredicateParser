package me.blvckbytes.item_predicate_parser.translation.keyed;

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

  @Override
  public LangKeyedPredicateType getPredicateType() {
    return LangKeyedPredicateType.AMOUNT;
  }
}
