package me.blvckbytes.item_predicate_parser.predicate;

public interface BinaryNode extends ItemPredicate {

  ItemPredicate getLHS();

  ItemPredicate getRHS();

}
