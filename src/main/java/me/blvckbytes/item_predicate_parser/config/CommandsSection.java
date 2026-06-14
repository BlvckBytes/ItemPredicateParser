package me.blvckbytes.item_predicate_parser.config;

import at.blvckbytes.cm_mapper.mapper.section.CSAlways;
import at.blvckbytes.cm_mapper.mapper.section.ConfigSection;
import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.util.logging.InterpreterLogger;
import me.blvckbytes.item_predicate_parser.command.hand.IPPHandCommandSection;
import me.blvckbytes.item_predicate_parser.command.main.IPPCommandSection;

@CSAlways
public class CommandsSection extends ConfigSection {

  public IPPCommandSection itemPredicateParser;
  public IPPHandCommandSection itemPredicateParserHand;

  public CommandsSection(InterpretationEnvironment baseEnvironment, InterpreterLogger interpreterLogger) {
    super(baseEnvironment, interpreterLogger);
  }
}
