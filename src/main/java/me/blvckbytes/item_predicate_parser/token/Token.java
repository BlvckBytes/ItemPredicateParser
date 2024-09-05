package me.blvckbytes.item_predicate_parser.token;

public interface Token {
  /**
   * Zero-based index of the beginning argument (in case of multi-arg strings) within
   * the command as dispatched by the user
   */
  int commandArgumentIndex();

  /**
   * Index of the first char which corresponds to this token, relative to its containing first argument
   */
  int firstCharIndex();

  String stringify();
}
