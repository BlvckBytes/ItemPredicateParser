package me.blvckbytes.item_predicate_parser.predicate;

import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

public class StringifyState {

  private final StringBuilder resultBuilder;
  private final @Nullable BiConsumer<ItemPredicate, StringBuilder> beforeEachHandler, afterEachHandler;

  public final boolean useTokens;

  public StringifyState(
    boolean useTokens,
    @Nullable BiConsumer<ItemPredicate, StringBuilder> beforeEachHandler,
    @Nullable BiConsumer<ItemPredicate, StringBuilder> afterEachHandler
  ) {
    this.useTokens = useTokens;
    this.beforeEachHandler = beforeEachHandler;
    this.afterEachHandler = afterEachHandler;
    this.resultBuilder = new StringBuilder();
  }

  public StringifyState(boolean useTokens) {
    this(useTokens, null, null);
  }

  public StringifyState appendPredicate(ItemPredicate predicate) {
    if (beforeEachHandler != null)
      beforeEachHandler.accept(predicate, resultBuilder);

    predicate.stringify(this);

    if (afterEachHandler != null)
      afterEachHandler.accept(predicate, resultBuilder);

    return this;
  }

  public void appendSpace() {
    resultBuilder.append(' ');
  }

  public void appendString(String input) {
    resultBuilder.append(input);
  }

  @Override
  public String toString() {
    return resultBuilder.toString();
  }
}
