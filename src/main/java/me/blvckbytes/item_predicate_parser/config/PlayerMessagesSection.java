package me.blvckbytes.item_predicate_parser.config;

import at.blvckbytes.cm_mapper.cm.ComponentMarkup;
import at.blvckbytes.cm_mapper.mapper.section.ConfigSection;
import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.util.logging.InterpreterLogger;

public class PlayerMessagesSection extends ConfigSection {

  public ComponentMarkup commandOnlyForPlayers;
  public ComponentMarkup pluginReloadedSuccess;
  public ComponentMarkup pluginReloadedError;
  public ComponentMarkup usageIppCommandAction;
  public ComponentMarkup usageIppLanguage;
  public ComponentMarkup predicateTestResult;
  public ComponentMarkup predicateParseError;
  public ComponentMarkup showingVariables;
  public ComponentMarkup unknownVariableName;
  public ComponentMarkup variablesTestNoResults;
  public ComponentMarkup variablesTestResults;
  public ComponentMarkup noItemInMainHand;
  public ComponentMarkup languageSelected;

  public ComponentMarkup missingPermissionIppCommand;

  public PlayerMessagesSection(InterpretationEnvironment baseEnvironment, InterpreterLogger interpreterLogger) {
    super(baseEnvironment, interpreterLogger);
  }
}
