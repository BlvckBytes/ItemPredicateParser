package me.blvckbytes.item_predicate_parser.config;

import me.blvckbytes.bukkitevaluable.section.ACommandSection;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;

public class ItemPredicateParserCommandSection extends ACommandSection {

  public static final String INITIAL_NAME = "itempredicateparser";

  public ItemPredicateParserCommandSection(EvaluationEnvironmentBuilder baseEnvironment) {
    super(INITIAL_NAME, baseEnvironment);
  }
}
