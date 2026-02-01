package me.blvckbytes.item_predicate_parser.config.variables_display;

import at.blvckbytes.cm_mapper.section.gui.PaginatedGuiSection;
import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.util.logging.InterpreterLogger;

public class VariablesDisplaySection extends PaginatedGuiSection<VariablesDisplayItemsSection> {

  public VariablesDisplaySection(InterpretationEnvironment baseEnvironment, InterpreterLogger interpreterLogger) {
    super(VariablesDisplayItemsSection.class, baseEnvironment, interpreterLogger);
  }
}
