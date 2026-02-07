package me.blvckbytes.item_predicate_parser.predicate;

public interface UnaryNode extends ItemPredicate {

  ItemPredicate getOperand();

}
