package me.blvckbytes.item_predicate_parser.token;

import me.blvckbytes.item_predicate_parser.parse.ParserInput;

public interface Token {
  int beginCommandArgumentIndex();

  int beginFirstCharIndex();

  int endCommandArgumentIndex();

  int endLastCharIndex();

  ParserInput parserInput();

  String stringify();
}
