package me.blvckbytes.item_predicate_parser.config;

import at.blvckbytes.cm_mapper.mapper.MappingError;
import at.blvckbytes.cm_mapper.mapper.section.CSIgnore;
import at.blvckbytes.cm_mapper.mapper.section.CSInlined;
import at.blvckbytes.cm_mapper.mapper.section.ConfigSection;
import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.util.logging.InterpreterLogger;
import me.blvckbytes.item_predicate_parser.translation.keyed.Variable;
import me.blvckbytes.item_predicate_parser.translation.keyed.VariableKey;

import java.lang.reflect.Field;
import java.util.*;

public class VariablesSection extends ConfigSection {

  public @CSInlined Map<String, VariableSection> variables;

  public @CSIgnore List<VariableKey> _variableKeys = new ArrayList<>();

  public VariablesSection(InterpretationEnvironment baseEnvironment, InterpreterLogger interpreterLogger) {
    super(baseEnvironment, interpreterLogger);
  }

  @Override
  public void afterParsing(List<Field> fields) throws Exception {
    super.afterParsing(fields);

    if (variables == null)
      return;

    var variableByNameLower = new HashMap<String, Variable>();

    for (var variableEntry : variables.entrySet()) {
      var defaultName = variableEntry.getKey();

      if (defaultName.contains(Variable.ENCLOSING_MARKER))
        throw new MappingError("Variable-names cannot contain \"" + Variable.ENCLOSING_MARKER + "\"");

      var variableSection = variableEntry.getValue();

      if (variableSection == null)
        variableSection = new VariableSection(baseEnvironment, interpreterLogger);

      var variable = new Variable(
        variableSection._icon,
        defaultName,
        variableSection._materials,
        variableSection._blockedMaterials,
        variableSection._parentNames,
        variableSection._names
      );

      _variableKeys.add(new VariableKey(variable));
      variableByNameLower.put(defaultName, variable);
    }

    variableByNameLower.values().forEach(variable -> {
      variable.resolveInheritance(variableByNameLower, new Stack<>());
    });
  }
}
