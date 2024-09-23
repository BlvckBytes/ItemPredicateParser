package me.blvckbytes.item_predicate_parser.translation.keyed;

public class NegationKey implements LangKeyed<NegationKey> {

  public static final NegationKey INSTANCE = new NegationKey();

  private NegationKey() {}

  @Override
  public String getLanguageFileKey() {
    return "custom.item-predicate-parser.negation-key";
  }

  @Override
  public NegationKey getWrapped() {
    return this;
  }

  @Override
  public LangKeyedPredicateType getPredicateType() {
    return LangKeyedPredicateType.NOT_A_PREDICATE;
  }
}
