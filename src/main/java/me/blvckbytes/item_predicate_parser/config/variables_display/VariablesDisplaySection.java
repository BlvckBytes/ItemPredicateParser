package me.blvckbytes.item_predicate_parser.config.variables_display;

import at.blvckbytes.cm_mapper.section.gui.PaginatedGuiSection;
import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.util.logging.InterpreterLogger;

public class VariablesDisplaySection extends PaginatedGuiSection<VariablesDisplayItemsSection> {

  public int maxTokenLineWidth = 35;

  public VariablesDisplaySection(Class<VariablesDisplayItemsSection> itemsSectionClass, InterpretationEnvironment baseEnvironment, InterpreterLogger interpreterLogger) {
    super(itemsSectionClass, baseEnvironment, interpreterLogger);
  }
}
