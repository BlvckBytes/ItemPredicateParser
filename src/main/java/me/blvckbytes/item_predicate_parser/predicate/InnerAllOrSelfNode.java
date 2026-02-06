package me.blvckbytes.item_predicate_parser.predicate;

import me.blvckbytes.item_predicate_parser.token.Token;
import me.blvckbytes.item_predicate_parser.translation.TranslatedLangKeyed;

public class InnerAllOrSelfNode extends InnerNode {

  public InnerAllOrSelfNode(Token token, TranslatedLangKeyed<?> translatedLangKeyed, ItemPredicate operand) {
    super(token, translatedLangKeyed, operand, InnerMode.ALL, true);
  }
}
