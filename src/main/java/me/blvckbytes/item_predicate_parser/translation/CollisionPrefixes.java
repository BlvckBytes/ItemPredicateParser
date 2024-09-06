package me.blvckbytes.item_predicate_parser.translation;

public record CollisionPrefixes(
  String forEnchantments,
  String forEffects,
  String forMaterials
) {
  public static CollisionPrefixes ENGLISH = new CollisionPrefixes(
    "[Enchantment]",
    "[Effect]",
    "[Material]"
  );

  public static CollisionPrefixes GERMAN = new CollisionPrefixes(
    "[Verzauberung]",
    "[Effekt]",
    "[Material]"
  );
}
