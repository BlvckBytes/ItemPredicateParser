package me.blvckbytes.item_predicate_parser.translation;

public record CollisionPrefixes(
  String forEnchantments,
  String forEffects,
  String forPotionTypes,
  String forMaterials,
  String forInstruments
) {
  public static CollisionPrefixes ENGLISH = new CollisionPrefixes(
    "[Enchantment]",
    "[Effect]",
    "[Potion-Type]",
    "[Material]",
    "[Instrument]"
  );

  public static CollisionPrefixes FRENCH = new CollisionPrefixes(
    "[Enchantement]",
    "[Effet]",
    "[Potion-Type]",
    "[Matériel]",
    "[Instrument]"
  );

  public static CollisionPrefixes GERMAN = new CollisionPrefixes(
    "[Verzauberung]",
    "[Effekt]",
    "[Trank-Typ]",
    "[Material]",
    "[Instrument]"
  );

  public static CollisionPrefixes CHINESE_SIMPLIFIED = new CollisionPrefixes(
    "[附魔]",
    "[效果]",
    "[Potion-Type]",
    "[材料]",
    "[乐器]"
  );
  
  public static CollisionPrefixes TURKISH = new CollisionPrefixes(
    "[Büyü]",
    "[Efekt]",
    "[Potion-Type]",
    "[Materyal]",
    "[Enstrüman]"
  );
}
