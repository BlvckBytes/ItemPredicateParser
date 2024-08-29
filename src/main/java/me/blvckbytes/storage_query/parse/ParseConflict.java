package me.blvckbytes.storage_query.parse;

public enum ParseConflict {
  /**
   * Expected an integer as an argument to the preceding search-pattern, but got something else
   */
  EXPECTED_INTEGER,
  /**
   * Expected an unquoted search-pattern, but got something else
   */
  EXPECTED_SEARCH_PATTERN,
  /**
   * The specified pattern did not fully apply to any existing translatable
   */
  NO_SEARCH_MATCH,
  /**
   * Either tried to put two or more strings into a single argument or began yet
   * another string within the same argument that terminated a previous multi-arg string,
   * or closed a string that has not been previously begun
   */
  MALFORMED_STRING_ARGUMENT,
  /**
   * Didn't close a previously began string until the very end
   */
  MISSING_STRING_TERMINATION,
  /**
   * The search-pattern matched on an instance of a translatable that is not yet
   * implemented; this should never happen in production.
   */
  UNIMPLEMENTED_TRANSLATABLE,
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
}
