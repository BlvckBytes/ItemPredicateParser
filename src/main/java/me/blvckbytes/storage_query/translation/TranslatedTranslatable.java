package me.blvckbytes.storage_query.translation;

import me.blvckbytes.storage_query.parse.SubstringIndices;
import org.bukkit.Translatable;

import java.util.List;

public class TranslatedTranslatable {

  public final TranslatableSource source;
  public final Translatable translatable;
  public final String translation;
  public final String normalizedName;
  public final List<SubstringIndices> partIndices;

  public int alphabeticalIndex = 0;

  public TranslatedTranslatable(
    TranslatableSource source,
    Translatable translatable,
    String translation,
    String normalizedName,
    List<SubstringIndices> partIndices
  ) {
    this.source = source;
    this.translatable = translatable;
    this.translation = translation;
    this.normalizedName = normalizedName;
    this.partIndices = partIndices;
  }

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
      SubstringIndices.forString(null, translation, SubstringIndices.LANGUAGE_FILE_DELIMITERS)
    );
  }

  @Override
  public String toString() {
    return normalizedName;
  }
}
