package me.blvckbytes.item_predicate_parser.translation.keyed;

import at.blvckbytes.cm_mapper.mapper.MappingError;
import me.blvckbytes.item_predicate_parser.translation.TranslationLanguage;
import me.blvckbytes.item_predicate_parser.translation.TranslationRegistry;
import org.bukkit.Material;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Variable {

  public static final String ENCLOSING_MARKER = "%";

  private enum VisitStatus {
    UNVISITED,
    VISITING,
    VISITED,
  }

  private VisitStatus status = VisitStatus.UNVISITED;

  public final String defaultName;

  public final Material icon;

  private final Set<Material> _materials;
  public final List<Material> materials;
  public final Set<Material> blockedMaterials;

  public final List<String> parentNames;
  public final List<Variable> parents;

  private final Set<Material> _inheritedMaterials;
  private List<Material> inheritedMaterials;

  private List<Material> effectiveMaterials;

  public final Map<TranslationLanguage, String> nameByLanguage;

  public Variable(
    Material icon,
    String defaultName,
    Set<Material> materials,
    Set<Material> blockedMaterials,
    Set<String> parentNames,
    Map<TranslationLanguage, String> nameByLanguage
  ) {
    if (defaultName.contains(ENCLOSING_MARKER))
      throw new IllegalStateException("Variable-names cannot contain \"" + ENCLOSING_MARKER + "\"");

    this.icon = icon;
    this.defaultName = defaultName;

    this._materials = Set.copyOf(materials);
    this.materials = List.copyOf(materials);
    this.blockedMaterials = Set.copyOf(blockedMaterials);

    this.parentNames = List.copyOf(parentNames);
    this.parents = new ArrayList<>();

    this.nameByLanguage = nameByLanguage;

    this._inheritedMaterials = new HashSet<>();
  }

  private void resolveMaterialNames(Collection<Material> materials, TranslationRegistry registry, Consumer<String> nameHandler) {
    for (var material : materials) {
      var translation = registry.getTranslationBySingleton(material);

      if (translation == null)
        translation = material.name();

      nameHandler.accept(translation);
    }
  }

  public void forEachMaterialName(TranslationRegistry registry, Consumer<String> nameHandler) {
    resolveMaterialNames(materials, registry, nameHandler);
  }

  public void forEachBlockedMaterialName(TranslationRegistry registry, Consumer<String> nameHandler) {
    resolveMaterialNames(blockedMaterials, registry, nameHandler);
  }

  public void forEachInheritedMaterialName(TranslationRegistry registry, Consumer<String> nameHandler) {
    resolveMaterialNames(inheritedMaterials, registry, nameHandler);
  }

  public List<Material> getEffectiveMaterials() {
    return effectiveMaterials;
  }

  public List<Material> getInheritedMaterials() {
    return inheritedMaterials;
  }

  public String getFinalName(TranslationLanguage language) {
    return ENCLOSING_MARKER + nameByLanguage.getOrDefault(language, this.defaultName) + ENCLOSING_MARKER;
  }

  public void resolveInheritance(Map<String, Variable> knownVariables, Stack<Variable> resolutionStack) {
    if (status == VisitStatus.VISITING)
      throw new MappingError("Detected loop: " + resolutionStack.stream().map(it -> it.defaultName).collect(Collectors.joining(" - ")));

    if (status == VisitStatus.VISITED)
      return;

    resolutionStack.push(this);

    status = VisitStatus.VISITING;

    for (var parentName : parentNames) {
      var parentVariable = knownVariables.get(parentName);

      if (parentVariable == null)
        throw new MappingError("Unknown parent \"" + parentName + "\" on \"" + defaultName + "\"");

      parentVariable.resolveInheritance(knownVariables, resolutionStack);

      for (var parentMaterial : parentVariable.getInheritedMaterials()) {
        if (_materials.contains(parentMaterial))
          continue;

        if (blockedMaterials.contains(parentMaterial))
          continue;

        _inheritedMaterials.add(parentMaterial);
      }

      for (var parentMaterial : parentVariable.materials) {
        if (_materials.contains(parentMaterial))
          continue;

        if (blockedMaterials.contains(parentMaterial))
          continue;

        _inheritedMaterials.add(parentMaterial);
      }

      this.parents.add(parentVariable);
    }

    status = VisitStatus.VISITED;
    resolutionStack.pop();

    inheritedMaterials = List.copyOf(_inheritedMaterials);

    effectiveMaterials = new ArrayList<>();
    effectiveMaterials.addAll(materials);
    effectiveMaterials.addAll(_inheritedMaterials);
  }
}
