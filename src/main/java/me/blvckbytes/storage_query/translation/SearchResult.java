package me.blvckbytes.storage_query.translation;

import me.blvckbytes.storage_query.parse.SearchWildcardPresence;

import java.util.List;

public record SearchResult (List<TranslatedTranslatable> result, SearchWildcardPresence wildcardPresence) {
  public SearchResult() {
    this(List.of(), SearchWildcardPresence.ABSENT);
  }
}
