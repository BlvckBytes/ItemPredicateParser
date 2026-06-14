package me.blvckbytes.item_predicate_parser.command.hand;

import at.blvckbytes.cm_mapper.section.command.CommandSection;
import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.util.logging.InterpreterLogger;

public class IPPHandCommandSection extends CommandSection {

  public static final String INITIAL_NAME = "itempredicateparserhand";

  public IPPHandCommandSection(InterpretationEnvironment baseEnvironment, InterpreterLogger interpreterLogger) {
    super(INITIAL_NAME, baseEnvironment, interpreterLogger);
  }
}
