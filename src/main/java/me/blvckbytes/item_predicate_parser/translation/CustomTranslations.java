package me.blvckbytes.item_predicate_parser.translation;

public record CustomTranslations(
  String deterioration,
  String negation,
  String disjunction,
  String conjunction,
  String exact,
  String amount,
  String innerSome,
  String innerAll
) {
  public static CustomTranslations ENGLISH = new CustomTranslations(
    "Deterioration",
    "not",
    "or",
    "and",
    "exact",
    "Amount",
    "inner-some",
    "inner-all"
  );

  public static CustomTranslations FRENCH = new CustomTranslations(
    "Deterioration",
    "non",
    "ou",
    "et",
    "exacte",
    "Quantité",
    "inner-some",
    "inner-all"
  );

  public static CustomTranslations GERMAN = new CustomTranslations(
    "Abnutzung",
    "nicht",
    "oder",
    "und",
    "exakt",
    "Anzahl",
    "innen-manche",
    "innen-alle"
  );

  public static CustomTranslations CHINESE_SIMPLIFIED = new CustomTranslations(
    "恶化",
    "不是",
    "或",
    "和",
    "精确",
    "数量",
    "inner-some",
    "inner-all"
  );

  public static CustomTranslations TURKISH = new CustomTranslations(
    "Bozulma",
    "hayır",
    "veya",
    "ve",
    "kesin",
    "Miktar",
    "inner-some",
    "inner-all"
  );
}
