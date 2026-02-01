package me.blvckbytes.item_predicate_parser.config;

import at.blvckbytes.cm_mapper.cm.ComponentMarkup;
import at.blvckbytes.cm_mapper.mapper.section.CSAlways;
import at.blvckbytes.cm_mapper.mapper.section.ConfigSection;
import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.util.logging.InterpreterLogger;
import me.blvckbytes.item_predicate_parser.config.variables_display.VariablesDisplaySection;
import me.blvckbytes.item_predicate_parser.translation.TranslationLanguage;

import java.util.Map;

@CSAlways
public class MainSection extends ConfigSection {

  public ComponentMarkup expandedPreview;
  public int maxCompletionsCount = 30;
  public ComponentMarkup maxCompletionsExceeded;
  public ComponentMarkup matchedPredicatePart;
  public ComponentMarkup mismatchedPredicatePart;
  public ComponentMarkup malformedPredicatePart;
  public ComponentMarkup remainingPredicatePart;
  public TranslationLanguage defaultSelectedLanguage;
  public CommandsSection commands;
  public Map<String, ComponentMarkup> parseConflicts;
  public VariablesSection variables;
  public VariablesDisplaySection variablesDisplay;
  public int variablesServerPort;
  public PlayerMessagesSection playerMessages;

  public MainSection(InterpretationEnvironment baseEnvironment, InterpreterLogger interpreterLogger) {
    super(baseEnvironment, interpreterLogger);
  }
}
