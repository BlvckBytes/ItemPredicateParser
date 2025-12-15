package me.blvckbytes.item_predicate_parser.predicate;

import me.blvckbytes.item_predicate_parser.token.Token;
import me.blvckbytes.item_predicate_parser.translation.TranslatedLangKeyed;

public class InnerSomeNode extends InnerNode {

  public InnerSomeNode(Token token, TranslatedLangKeyed<?> translatedLangKeyed, ItemPredicate operand) {
    super(token, translatedLangKeyed, operand, InnerMode.SOME);
  }
}
