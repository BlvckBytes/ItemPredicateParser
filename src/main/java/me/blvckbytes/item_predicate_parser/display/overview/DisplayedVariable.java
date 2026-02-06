package me.blvckbytes.item_predicate_parser.display.overview;

import org.bukkit.Material;

import java.util.List;

public record DisplayedVariable(
  Material icon,
  String displayName,
  List<String> materialDisplayNames,
  List<String> blockedMaterialDisplayNames,
  List<String> parentDisplayNames,
  List<String> inheritedMaterialDisplayNames
) {}
