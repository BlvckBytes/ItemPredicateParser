package me.blvckbytes.item_predicate_parser;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @param suggestions Suggestions based on the last parsed token; null for non-syllable tokens
 * @param expandedPreviewOrError Either the expanded preview of the parsed predicate, or the error-message
 *                               if the error-flag is true; null on empty input or missing config-value
 * @param didParseErrorOccur Flag signalling whether parsing the predicate used to create the expanded preview
 *                           resulted in a parser error, which will become the preview's alternate value
 */
public record CompletionResult(
  @Nullable List<String> suggestions,
  @Nullable String expandedPreviewOrError,
  boolean didParseErrorOccur
) {}
