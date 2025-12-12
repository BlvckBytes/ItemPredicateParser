package me.blvckbytes.item_predicate_parser.config;

import me.blvckbytes.bbconfigmapper.sections.AConfigSection;
import me.blvckbytes.bbconfigmapper.sections.CSAlways;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import me.blvckbytes.item_predicate_parser.config.variables_display.VariablesDisplaySection;
import me.blvckbytes.item_predicate_parser.translation.TranslationLanguage;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@CSAlways
public class MainSection extends AConfigSection {

  public @Nullable BukkitEvaluable expandedPreview;
  public BukkitEvaluable maxCompletionsCount;
  public @Nullable BukkitEvaluable maxCompletionsExceeded;
  public @Nullable BukkitEvaluable inputNonHighlightPrefix;
  public @Nullable BukkitEvaluable inputHighlightPrefix;
  public TranslationLanguage defaultSelectedLanguage;
  public CommandsSection commands;
  public Map<String, BukkitEvaluable> parseConflicts;
  public VariablesSection variables;
  public VariablesDisplaySection variablesDisplay;

  @CSAlways
  public PlayerMessagesSection playerMessages;

  public MainSection(EvaluationEnvironmentBuilder baseEnvironment) {
    super(baseEnvironment);

    maxCompletionsCount = new BukkitEvaluable(30, null, null);
  }
}
