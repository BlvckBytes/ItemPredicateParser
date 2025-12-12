package me.blvckbytes.item_predicate_parser.config.variables_display;

import me.blvckbytes.bbconfigmapper.sections.AConfigSection;
import me.blvckbytes.bbconfigmapper.sections.CSAlways;
import me.blvckbytes.bukkitevaluable.IItemBuildable;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import me.blvckbytes.item_predicate_parser.config.display_common.GuiItemStackSection;

@CSAlways
public class VariablesDisplayItemsSection extends AConfigSection {

  public GuiItemStackSection previousPage;
  public GuiItemStackSection nextPage;
  public GuiItemStackSection filler;
  public IItemBuildable variable;

  public VariablesDisplayItemsSection(EvaluationEnvironmentBuilder baseEnvironment) {
    super(baseEnvironment);
  }
}
