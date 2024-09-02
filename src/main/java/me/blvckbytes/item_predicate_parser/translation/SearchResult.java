package me.blvckbytes.item_predicate_parser.translation;

import java.util.List;

public record SearchResult (
  List<TranslatedTranslatable> result,
  boolean isWildcardPresent
) {}
