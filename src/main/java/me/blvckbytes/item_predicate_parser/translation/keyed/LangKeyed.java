package me.blvckbytes.item_predicate_parser.translation.keyed;

public interface LangKeyed<T> {

  String getLanguageFileKey();

  T getWrapped();

}
