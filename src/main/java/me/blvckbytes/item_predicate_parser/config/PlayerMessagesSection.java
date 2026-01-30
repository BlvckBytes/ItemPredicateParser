package me.blvckbytes.item_predicate_parser.config;

import at.blvckbytes.cm_mapper.cm.ComponentMarkup;
import at.blvckbytes.cm_mapper.mapper.section.ConfigSection;
import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.util.logging.InterpreterLogger;
import org.jetbrains.annotations.Nullable;

public class PlayerMessagesSection extends ConfigSection {

  public @Nullable ComponentMarkup commandOnlyForPlayers;
  public @Nullable ComponentMarkup pluginReloadedSuccess;
  public @Nullable ComponentMarkup pluginReloadedError;
  public @Nullable ComponentMarkup usageIppCommandAction;
  public @Nullable ComponentMarkup usageIppLanguageCommandLanguage;
  public @Nullable ComponentMarkup predicateTestResult;
  public @Nullable ComponentMarkup predicateParseError;
  public @Nullable ComponentMarkup showingVariables;
  public @Nullable ComponentMarkup unknownVariableName;
  public @Nullable ComponentMarkup variablesTestNoResults;
  public @Nullable ComponentMarkup variablesTestResults;
  public @Nullable ComponentMarkup noItemInMainHand;
  public @Nullable ComponentMarkup languageSelected;

  public @Nullable ComponentMarkup missingPermissionIppCommand;

  public PlayerMessagesSection(InterpretationEnvironment baseEnvironment, InterpreterLogger interpreterLogger) {
    super(baseEnvironment, interpreterLogger);
  }
}
