package me.blvckbytes.item_predicate_parser.translation;

public enum TranslationLanguage {

  // NOTE: There is a reason as to why only select languages are supported, and their collision-prefixes
  //       as well as their custom-key-translations are hardcoded: Firstly, expressions shall behave
  //       uniformly across all consumers of this API, and secondly, I want to make sure that there are
  //       no parsing- or comparison-errors when it comes to Unicode-symbols.

  ENGLISH_US("en_us.json", CollisionPrefixes.ENGLISH, CustomTranslations.ENGLISH),
  ENGLISH_GB("en_gb.json", CollisionPrefixes.ENGLISH, CustomTranslations.ENGLISH),
  GERMAN_DE("de_de.json", CollisionPrefixes.GERMAN, CustomTranslations.GERMAN),
  GERMAN_AT("de_at.json", CollisionPrefixes.GERMAN, CustomTranslations.GERMAN),
  GERMAN_CH("de_ch.json", CollisionPrefixes.GERMAN, CustomTranslations.GERMAN),
  ;

  public final String assetFileName;
  public final CollisionPrefixes collisionPrefixes;
  public final CustomTranslations customTranslations;

  TranslationLanguage(
    String assetFileName,
    CollisionPrefixes collisionPrefixes,
    CustomTranslations customTranslations
  ) {
    this.assetFileName = assetFileName;
    this.collisionPrefixes = collisionPrefixes;
    this.customTranslations = customTranslations;
  }
}
