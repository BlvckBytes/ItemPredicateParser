package me.blvckbytes.item_predicate_parser.translation;

import com.google.gson.JsonObject;

public record CustomTranslations(
  String deterioration,
  String negation,
  String disjunction,
  String conjunction,
  String exact,
  String amount
) {
  public static CustomTranslations ENGLISH = new CustomTranslations(
    "Deterioration",
    "not",
    "or",
    "and",
    "exact",
    "Amount"
  );

  public static CustomTranslations GERMAN = new CustomTranslations(
    "Abnutzung",
    "nicht",
    "oder",
    "und",
    "exakt",
    "Anzahl"
  );

  public void apply(JsonObject languageFile) {
    languageFile.addProperty(DeteriorationKey.INSTANCE.getTranslationKey(), deterioration);
    languageFile.addProperty(NegationKey.INSTANCE.getTranslationKey(), negation);
    languageFile.addProperty(DisjunctionKey.INSTANCE.getTranslationKey(), disjunction);
    languageFile.addProperty(ConjunctionKey.INSTANCE.getTranslationKey(), conjunction);
    languageFile.addProperty(ExactKey.INSTANCE.getTranslationKey(), exact);
    languageFile.addProperty(AmountKey.INSTANCE.getTranslationKey(), amount);
  }
}
