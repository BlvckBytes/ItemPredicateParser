package me.blvckbytes.item_predicate_parser.config;

import me.blvckbytes.gpeee.functions.AExpressionFunction;
import me.blvckbytes.gpeee.functions.ExpressionFunctionArgument;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import me.blvckbytes.item_predicate_parser.predicate.ItemPredicate;
import me.blvckbytes.item_predicate_parser.predicate.StringifyState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HighlightPredicateFunction extends AExpressionFunction {

  private final ItemPredicate rootNode;
  private final @Nullable ItemPredicate failureNode;

  public HighlightPredicateFunction(ItemPredicate rootNode, @Nullable ItemPredicate failureNode) {
    this.rootNode = rootNode;
    this.failureNode = failureNode;
  }

  @Override
  public Object apply(IEvaluationEnvironment environment, List<@Nullable Object> args) {
    String matchingPrefix = nonNull(args, 0);
    String failurePrefix = nonNull(args, 1);

    return new StringifyState(
      false,
      (node, output) -> {
        if (failureNode == null) {
          output.append(matchingPrefix);
          return;
        }

        output.append(node == failureNode || failureNode.isTransitiveParentTo(node) ? failurePrefix : matchingPrefix);
      },
      null
    )
      .appendPredicate(rootNode)
      .toString();
  }

  @Override
  public @Nullable List<ExpressionFunctionArgument> getArguments() {
    return List.of(
      new ExpressionFunctionArgument("matching_prefix", "Prefix prepended to matching predicate-nodes", true, String.class),
      new ExpressionFunctionArgument("failure_prefix", "Prefix prepended to failing predicate-nodes", true, String.class)
    );
  }
}
