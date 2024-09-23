package me.blvckbytes.item_predicate_parser.translation.keyed;

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

  @Override
  public LangKeyedPredicateType getPredicateType() {
    return LangKeyedPredicateType.NOT_A_PREDICATE;
  }
}
