package me.blvckbytes.item_predicate_parser.predicate.stringify;

import me.blvckbytes.item_predicate_parser.predicate.ItemPredicate;

import java.util.function.Consumer;

public interface StringifyHandler {

  void beginNonTerminalNode(ItemPredicate predicate);

  void endNonTerminalNode(ItemPredicate predicate);

  void stringify(ItemPredicate predicate, Consumer<StringifyOutput> stringifier);

  boolean useTokens();

}
