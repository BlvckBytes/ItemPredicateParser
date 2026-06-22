package me.blvckbytes.item_predicate_parser.predicate;

import me.blvckbytes.item_predicate_parser.predicate.stringify.StringifyHandler;
import me.blvckbytes.item_predicate_parser.token.IntegerToken;
import me.blvckbytes.item_predicate_parser.token.Token;
import me.blvckbytes.item_predicate_parser.translation.TranslatedLangKeyed;
import me.blvckbytes.item_predicate_parser.translation.keyed.RepairCostKey;
import org.bukkit.inventory.meta.Repairable;
import org.jetbrains.annotations.Nullable;

public record RepairCostPredicate(
  Token token,
  TranslatedLangKeyed<RepairCostKey> translatedLangKeyed,
  IntegerToken amountArgument
) implements ItemPredicate {

  @Override
  public @Nullable ItemPredicate testForFailure(PredicateState state) {
    if (!(state.getMeta() instanceof Repairable repairable))
      return this;

    var repairCost = repairable.hasRepairCost() ? repairable.getRepairCost() : 0;

    if (amountArgument.matches(repairCost))
      return null;

    return this;
  }

  @Override
  public void stringify(StringifyHandler handler) {
    handler.stringify(this, output -> {
      if (handler.useTokens())
        output.appendString(token.stringify());
      else
        output.appendString(translatedLangKeyed.normalizedPrefixedTranslation);

      output.appendSpace();
      output.appendString(amountArgument.stringify());
    });
  }


  @Override
  public boolean equals(Object other) {
    if (!(other instanceof RepairCostPredicate otherPredicate))
      return false;

    return amountArgument.equals(otherPredicate.amountArgument);
  }
}
