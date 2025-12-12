package me.blvckbytes.item_predicate_parser.translation.keyed;

import me.blvckbytes.item_predicate_parser.translation.TranslationLanguage;
import org.bukkit.Material;

import java.util.List;
import java.util.Map;

public class Variable {

  public static final String ENCLOSING_MARKER = "%";

  private final String defaultName;

  public final Material icon;
  public final List<Material> materials;
  public final Map<TranslationLanguage, String> nameByLanguage;

  public Variable(Material icon, String defaultName, List<Material> materials, Map<TranslationLanguage, String> nameByLanguage) {
    if (defaultName.contains(ENCLOSING_MARKER))
      throw new IllegalStateException("Variable-names cannot contain \"" + ENCLOSING_MARKER + "\"");

    this.icon = icon;
    this.defaultName = defaultName;
    this.materials = materials;
    this.nameByLanguage = nameByLanguage;
  }

  public String getFinalName(TranslationLanguage language) {
    return ENCLOSING_MARKER + nameByLanguage.getOrDefault(language, this.defaultName) + ENCLOSING_MARKER;
  }
}
