package me.blvckbytes.storage_query.translation;

import me.blvckbytes.storage_query.parse.SubstringIndices;
import org.bukkit.Translatable;

import java.util.List;

public record TranslatedTranslatable(
  TranslatableSource source,
  Translatable translatable,
  String translation,
  String normalizedName,
  List<SubstringIndices> partIndices
) {
  public TranslatedTranslatable(
    TranslatableSource source,
    Translatable translatable,
    String translation
  ) {
    this(
      source,
      translatable,
      translation,
      translation
        .replace(' ', '-')
        .replace('_', '-')
      ,
      SubstringIndices.forString(translation, SubstringIndices.LANGUAGE_FILE_DELIMITERS)
    );
  }
}
