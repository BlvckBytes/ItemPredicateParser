package me.blvckbytes.item_predicate_parser.parse;

@FunctionalInterface
public interface UnmatchedSyllableConsumer {

  void accept(Syllables holder, int syllable);

}
