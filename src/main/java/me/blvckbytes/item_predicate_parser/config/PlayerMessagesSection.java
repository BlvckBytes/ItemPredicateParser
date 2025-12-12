package me.blvckbytes.item_predicate_parser.config;

import me.blvckbytes.bbconfigmapper.sections.AConfigSection;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import org.jetbrains.annotations.Nullable;

public class PlayerMessagesSection extends AConfigSection {

  public @Nullable BukkitEvaluable commandOnlyForPlayers;
  public @Nullable BukkitEvaluable pluginReloadedSuccess;
  public @Nullable BukkitEvaluable pluginReloadedError;
  public @Nullable BukkitEvaluable usageIppCommandAction;
  public @Nullable BukkitEvaluable usageIppTestCommandLanguage;
  public @Nullable BukkitEvaluable usageIppVariablesCommandLanguage;
  public @Nullable BukkitEvaluable predicateTestResult;
  public @Nullable BukkitEvaluable predicateParseError;
  public @Nullable BukkitEvaluable showingVariables;
  public @Nullable BukkitEvaluable unknownVariableName;
  public @Nullable BukkitEvaluable variablesTestNoResults;
  public @Nullable BukkitEvaluable variablesTestResults;
  public @Nullable BukkitEvaluable noItemInMainHand;

  public @Nullable BukkitEvaluable missingPermissionIppTestCommand;
  public @Nullable BukkitEvaluable missingPermissionIppVariablesCommand;
  public @Nullable BukkitEvaluable missingPermissionIppReloadCommand;
  public @Nullable BukkitEvaluable missingPermissionIppCommand;

  public PlayerMessagesSection(EvaluationEnvironmentBuilder baseEnvironment) {
    super(baseEnvironment);
  }
}
