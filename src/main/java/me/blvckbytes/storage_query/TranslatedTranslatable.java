package me.blvckbytes.storage_query;

import org.bukkit.Translatable;

import java.util.List;
import java.util.Locale;

public record TranslatedTranslatable(
  Translatable translatable,
  String translation,
  String translationLower,
  String normalizedName,
  List<SubstringIndices> partIndices
) {
  public TranslatedTranslatable(Translatable translatable, Locale locale, String translation) {
    this(
            translatable,
            translation,
            translation.toLowerCase(locale),
            translation
                    .replace(' ', '-')
                    .replace('_', '-')
            ,
            SubstringIndices.forString(translation, SubstringIndices.LANGUAGE_FILE_DELIMITERS)
    );
  }
}
