package me.blvckbytes.storage_query.translation;

import me.blvckbytes.storage_query.parse.SubstringIndices;
import org.bukkit.Translatable;

import java.util.List;

public record TranslatedTranslatable(
  Translatable translatable,
  String translation,
  String normalizedName,
  List<SubstringIndices> partIndices
) {
  public TranslatedTranslatable(Translatable translatable, String translation) {
    this(
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
