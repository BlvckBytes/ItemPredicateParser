package me.blvckbytes.item_predicate_parser.translation;

public record CollisionPrefixes(
  String forEnchantments,
  String forEffects,
  String forMaterials,
  String forInstruments
) {
  public static CollisionPrefixes ENGLISH = new CollisionPrefixes(
    "[Enchantment]",
    "[Effect]",
    "[Material]",
    "[Instrument]"
  );

  public static CollisionPrefixes GERMAN = new CollisionPrefixes(
    "[Verzauberung]",
    "[Effekt]",
    "[Material]",
    "[Instrument]"
  );
}
