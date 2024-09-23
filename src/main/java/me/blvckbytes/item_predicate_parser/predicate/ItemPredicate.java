package me.blvckbytes.item_predicate_parser.predicate;

public interface ItemPredicate {

  boolean test(PredicateState state);

  /**
   * Stringifies the predicate to represent the fully-expanded version of it's previously
   * parsed arguments in the same style, i.e. space-separated values
   * @param useTokens When using tokens, stringification possibly yields abbreviations as
   *                  entered; otherwise, full translations will be made use of.
   */
  String stringify(boolean useTokens);
}
