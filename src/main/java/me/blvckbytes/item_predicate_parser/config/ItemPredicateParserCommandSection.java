package me.blvckbytes.item_predicate_parser.config;

import at.blvckbytes.cm_mapper.mapper.MappingError;
import at.blvckbytes.cm_mapper.section.command.CommandSection;
import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.util.logging.InterpreterLogger;

import java.lang.reflect.Field;
import java.util.List;

public class ItemPredicateParserCommandSection extends CommandSection {

  public static final String INITIAL_NAME = "itempredicateparser";

  public int predicateInteractionExpirySeconds;

  public ItemPredicateParserCommandSection(InterpretationEnvironment baseEnvironment, InterpreterLogger interpreterLogger) {
    super(INITIAL_NAME, baseEnvironment, interpreterLogger);
  }

  @Override
  public void afterParsing(List<Field> fields) throws Exception {
    super.afterParsing(fields);

    if (predicateInteractionExpirySeconds <= 0)
      throw new MappingError("\"predicateInteractionExpirySeconds\" cannot be less than or equal to zero");
  }
}
