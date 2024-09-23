package me.blvckbytes.item_predicate_parser.translation;

import com.google.gson.JsonObject;
import me.blvckbytes.item_predicate_parser.translation.keyed.*;

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
    languageFile.addProperty(DeteriorationKey.INSTANCE.getLanguageFileKey(), deterioration);
    languageFile.addProperty(NegationKey.INSTANCE.getLanguageFileKey(), negation);
    languageFile.addProperty(DisjunctionKey.INSTANCE.getLanguageFileKey(), disjunction);
    languageFile.addProperty(ConjunctionKey.INSTANCE.getLanguageFileKey(), conjunction);
    languageFile.addProperty(ExactKey.INSTANCE.getLanguageFileKey(), exact);
    languageFile.addProperty(AmountKey.INSTANCE.getLanguageFileKey(), amount);
  }
}
