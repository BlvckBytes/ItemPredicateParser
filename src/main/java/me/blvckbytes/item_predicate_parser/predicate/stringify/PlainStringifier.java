package me.blvckbytes.item_predicate_parser.predicate.stringify;

import me.blvckbytes.item_predicate_parser.predicate.ItemPredicate;

import java.util.function.Consumer;

public class PlainStringifier implements StringifyHandler, StringifyOutput {

  private final StringBuilder resultBuilder;

  private final boolean useTokens;

  private PlainStringifier(boolean useTokens) {
    this.useTokens = useTokens;
    this.resultBuilder = new StringBuilder();
  }

  public static String stringify(ItemPredicate predicate, boolean useTokens) {
    var stringifier = new PlainStringifier(useTokens);
    predicate.stringify(stringifier);
    return stringifier.toString();
  }

  @Override
  public void stringify(ItemPredicate predicate, Consumer<StringifyOutput> stringifier) {
    stringifier.accept(this);
  }

  @Override
  public boolean useTokens() {
    return useTokens;
  }

  @Override
  public void appendSpace() {
    resultBuilder.append(' ');
  }

  @Override
  public void appendString(String input) {
    resultBuilder.append(input);
  }

  @Override
  public String toString() {
    return resultBuilder.toString();
  }
}
