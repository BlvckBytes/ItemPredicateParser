package me.blvckbytes.item_predicate_parser.config;

import com.cryptomorin.xseries.XMaterial;
import me.blvckbytes.bbconfigmapper.MappingError;
import me.blvckbytes.bbconfigmapper.sections.AConfigSection;
import me.blvckbytes.bbconfigmapper.sections.CSIgnore;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import me.blvckbytes.item_predicate_parser.translation.TranslationLanguage;
import me.blvckbytes.item_predicate_parser.translation.keyed.Variable;
import org.bukkit.Material;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VariableSection extends AConfigSection {

  public String icon;
  public @CSIgnore Material _icon = Material.BARRIER;

  public List<String> materials;
  public @CSIgnore List<Material> _materials = new ArrayList<>();

  public Map<String, String> names;
  public @CSIgnore Map<TranslationLanguage, String> _names = new HashMap<>();

  public VariableSection(EvaluationEnvironmentBuilder baseEnvironment) {
    super(baseEnvironment);
  }

  @Override
  public void afterParsing(List<Field> fields) throws Exception {
    super.afterParsing(fields);

    if (icon != null) {
      var xMaterial = XMaterial.matchXMaterial(icon);

      if (xMaterial.isEmpty())
        throw new MappingError("Unknown icon-material \"" + icon + "\"");

      _icon = xMaterial.get().get();
    }

    if (materials != null) {
      for (var material : materials) {
        var xMaterial = XMaterial.matchXMaterial(material);

        if (xMaterial.isEmpty())
          throw new MappingError("Unknown material \"" + material + "\"");

        _materials.add(xMaterial.get().get());
      }
    }

    if (names != null) {
      for (var nameEntry : names.entrySet()) {
        var name = nameEntry.getValue();

        if (name.contains(Variable.ENCLOSING_MARKER))
          throw new MappingError("Variable-names cannot contain \"" + Variable.ENCLOSING_MARKER + "\"");

        var targetLanguage = nameEntry.getKey();
        var foundMatches = false;

        for (var language : TranslationLanguage.values()) {
          if (!language.name().toLowerCase().startsWith(targetLanguage.toLowerCase()))
            continue;

          if (_names.put(language, name) != null)
            throw new MappingError("Duplicate name for language \"" + language + "\"");

          foundMatches = true;
        }

        if (!foundMatches)
          throw new MappingError("No known language starts with \"" + targetLanguage + "\"");
      }
    }
  }
}
