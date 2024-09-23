package me.blvckbytes.item_predicate_parser.translation.keyed;

public class DeteriorationKey implements LangKeyed<DeteriorationKey> {

  public static final DeteriorationKey INSTANCE = new DeteriorationKey();

  private DeteriorationKey() {}

  @Override
  public String getLanguageFileKey() {
    return "custom.item-predicate-parser.deterioration-key";
  }

  @Override
  public DeteriorationKey getWrapped() {
    return this;
  }

  @Override
  public LangKeyedPredicateType getPredicateType() {
    return LangKeyedPredicateType.DETERIORATION;
  }
}
