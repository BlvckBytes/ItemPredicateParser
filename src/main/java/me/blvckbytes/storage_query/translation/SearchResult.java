package me.blvckbytes.storage_query.translation;

import java.util.List;

public record SearchResult (
  List<TranslatedTranslatable> result,
  boolean isWildcardPresent
) {}
