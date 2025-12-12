package me.blvckbytes.item_predicate_parser.config.variables_display;

import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import me.blvckbytes.item_predicate_parser.config.display_common.PaginatedGuiSection;

public class VariablesDisplaySection extends PaginatedGuiSection<VariablesDisplayItemsSection> {

  public int maxTokenLineWidth = 35;

  public VariablesDisplaySection(EvaluationEnvironmentBuilder baseEnvironment) {
    super(VariablesDisplayItemsSection.class, baseEnvironment);
  }
}
