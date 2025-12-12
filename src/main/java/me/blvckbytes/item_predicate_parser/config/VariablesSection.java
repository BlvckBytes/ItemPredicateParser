package me.blvckbytes.item_predicate_parser.config;

import me.blvckbytes.bbconfigmapper.MappingError;
import me.blvckbytes.bbconfigmapper.sections.AConfigSection;
import me.blvckbytes.bbconfigmapper.sections.CSIgnore;
import me.blvckbytes.bbconfigmapper.sections.CSInlined;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import me.blvckbytes.item_predicate_parser.translation.keyed.Variable;
import me.blvckbytes.item_predicate_parser.translation.keyed.VariableKey;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VariablesSection extends AConfigSection {

  public @CSInlined Map<String, VariableSection> variables;

  public @CSIgnore List<VariableKey> _variableKeys = new ArrayList<>();

  public VariablesSection(EvaluationEnvironmentBuilder baseEnvironment) {
    super(baseEnvironment);
  }

  @Override
  public void afterParsing(List<Field> fields) throws Exception {
    super.afterParsing(fields);

    if (variables == null)
      return;

    for (var variableEntry : variables.entrySet()) {
      var defaultName = variableEntry.getKey();

      if (defaultName.contains(Variable.ENCLOSING_MARKER))
        throw new MappingError("Variable-names cannot contain \"" + Variable.ENCLOSING_MARKER + "\"");

      var variableSection = variableEntry.getValue();

      if (variableSection == null)
        variableSection = new VariableSection(getBaseEnvironment());

      var variable = new Variable(defaultName, variableSection._materials, variableSection._names);

      _variableKeys.add(new VariableKey(variable));
    }
  }
}
