package me.blvckbytes.item_predicate_parser.parse;

public enum ParseConflict {
  /**
   * Expected the current token to be an integer, as it started out that way, but was malformed after all
   */
  EXPECTED_CORRECT_INTEGER,
  /**
   * Max. 3 blocks, separated by colon, are allowed when using time-notation
   */
  TOO_MANY_TIME_NOTATION_BLOCKS,
  /**
   * The current predicate expects a non-omit-able integer argument
   */
  EXPECTED_FOLLOWING_INTEGER,
  /**
   * Expected an unquoted search-pattern, but got something else
   */
  EXPECTED_SEARCH_PATTERN,
  /**
   * The specified pattern did not fully apply to any existing translatable
   */
  NO_SEARCH_MATCH,
  /**
   * Didn't close a previously began string until the very end
   */
  MISSING_STRING_TERMINATION,
  /**
   * Provided more than one search pattern wildcard (?) within a single argument
   */
  MULTIPLE_SEARCH_PATTERN_WILDCARDS,
  /**
   * Provided only a search pattern wildcard without any other syllables
   */
  ONLY_SEARCH_PATTERN_WILDCARD,
  /**
   * An integer token with time notation has been passed to a predicate which has
   * nothing to do with time.
   */
  DOES_NOT_ACCEPT_TIME_NOTATION,
  /**
   * An integer token with comparison notation has been passed to a predicate which
   * does not offer range comparison functionality
   */
  DOES_NOT_ACCEPT_NON_EQUALS_COMPARISON,
  /**
   * And/Not/Or/Exact all demand a right-hand-side expression
   */
  EXPECTED_EXPRESSION_AFTER_OPERATOR,
  /**
   * A closing parenthesis has been used to introduce a new parentheses-group
   */
  EXPECTED_OPENING_PARENTHESIS,
  /**
   * A previously introduced parentheses-group is missing its termination
   */
  EXPECTED_CLOSING_PARENTHESIS,
  /**
   * Quoted strings which are either completely empty or consist of only whitespace
   * are useless when searching for text-content, so they may not reach execution
   */
  EMPTY_OR_BLANK_QUOTED_STRING
}
