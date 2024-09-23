package me.blvckbytes.item_predicate_parser.translation.keyed;

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

  @Override
  public LangKeyedPredicateType getPredicateType() {
    return LangKeyedPredicateType.NOT_A_PREDICATE;
  }
}
