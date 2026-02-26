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
  public ComponentMarkup accessDeniedForBlock;
  public ComponentMarkup blockDoesNotSupportPredicates;
  public ComponentMarkup blockHasPredicateError;
  public ComponentMarkup blockHasNoPredicateSet;
  public ComponentMarkup getPredicateInitialized;
  public ComponentMarkup getPredicateFromBlock;
  public ComponentMarkup setPredicateInitialized;
  public ComponentMarkup setPredicateOnBlock;
  public ComponentMarkup setPredicateMissingLanguage;
  public ComponentMarkup setPredicateUnknownLanguage;
  public ComponentMarkup setPredicateEmpty;
  public ComponentMarkup removePredicateInitialized;
  public ComponentMarkup removePredicateFromBlock;
  public ComponentMarkup predicateInteractionExpired;
  public ComponentMarkup predicateInteractionMultiModeActionBarSignal;
  public ComponentMarkup predicateInteractionMultiModeEntered;
  public ComponentMarkup predicateInteractionMultiModeExited;

  public PlayerMessagesSection(InterpretationEnvironment baseEnvironment, InterpreterLogger interpreterLogger) {
    super(baseEnvironment, interpreterLogger);
  }
}
