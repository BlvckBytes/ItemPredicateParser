package me.blvckbytes.item_predicate_parser.predicate.stringify;

import at.blvckbytes.cm_mapper.ConfigKeeper;
import at.blvckbytes.component_markup.constructor.SlotType;
import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import me.blvckbytes.item_predicate_parser.config.MainSection;
import me.blvckbytes.item_predicate_parser.predicate.ItemPredicate;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class TokenHighlighter implements StringifyHandler {

  private final ConfigKeeper<MainSection> config;
  private final ItemPredicate failedPredicate;

  private @Nullable Component result;

  private TokenHighlighter(ConfigKeeper<MainSection> config, ItemPredicate failedPredicate) {
    this.config = config;
    this.failedPredicate = failedPredicate;
  }

  public static Component highlightFailure(
    ConfigKeeper<MainSection> config,
    ItemPredicate rootPredicate,
    ItemPredicate failedPredicate
  ) {
    var highlighter = new TokenHighlighter(config, failedPredicate);
    rootPredicate.stringify(highlighter);

    if (highlighter.result == null)
      throw new IllegalStateException("The root-predicate did not append anything to the output");

    return highlighter.result;
  }

  @Override
  public void stringify(ItemPredicate predicate, Consumer<StringifyOutput> stringifier) {
    var representation = predicate == failedPredicate
      ? config.rootSection.mismatchedPredicatePart
      : config.rootSection.matchedPredicatePart;

    var contents = new StringBuilder();

    stringifier.accept(new StringifyOutput() {

      @Override
      public void appendSpace() {
        contents.append(' ');
      }

      @Override
      public void appendString(String input) {
        contents.append(input);
      }
    });

    var renderResult = representation.interpret(
      SlotType.SINGLE_LINE_CHAT,
      new InterpretationEnvironment()
        .withVariable("predicate", contents.toString())
    ).get(0);

    if (result == null) {
      result = renderResult;
      return;
    }

    result = result.append(renderResult);
  }

  @Override
  public boolean useTokens() {
    return true;
  }
}
