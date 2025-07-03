package me.blvckbytes.item_predicate_parser.translation;

import me.blvckbytes.syllables_matcher.WildcardMode;

import java.util.List;

public record SearchResult (
  List<TranslatedLangKeyed<?>> result,
  WildcardMode wildcardMode
) {}
