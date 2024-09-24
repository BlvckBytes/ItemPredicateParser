package me.blvckbytes.item_predicate_parser.translation;

import com.google.gson.JsonObject;
import me.blvckbytes.item_predicate_parser.predicate.ItemPredicate;
import me.blvckbytes.item_predicate_parser.token.Token;
import me.blvckbytes.item_predicate_parser.translation.keyed.*;
import org.jetbrains.annotations.Nullable;

public interface IVersionDependentCode {

  Iterable<? extends LangKeyed<?>> getEnchantments();
  Iterable<? extends LangKeyed<?>> getEffects();
  Iterable<? extends LangKeyed<?>> getItemMaterials(JsonObject languageJson);

  @Nullable Iterable<? extends LangKeyed<?>> getInstruments();

  @Nullable ItemPredicate makeInstrumentPredicate(Token token, TranslatedLangKeyed<?> translation);

}
