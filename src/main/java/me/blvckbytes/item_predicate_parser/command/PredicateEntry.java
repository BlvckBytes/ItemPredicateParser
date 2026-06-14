package me.blvckbytes.item_predicate_parser.command;

import at.blvckbytes.component_markup.markup.interpreter.DirectFieldAccess;
import me.blvckbytes.item_predicate_parser.PredicateHelper;
import me.blvckbytes.item_predicate_parser.event.PredicateAndLanguage;
import org.bukkit.entity.Player;

import java.util.Set;

public record PredicateEntry(
  String setCommand,
  String predicate
) implements DirectFieldAccess {

  public static PredicateEntry fromPredicateAndLanguage(
    PredicateAndLanguage predicateAndLanguage,
    Player player,
    String label,
    PredicateHelper predicateHelper
  ) {
    var setCommand = "/" + label;

    if (predicateHelper.getSelectedLanguage(player) == predicateAndLanguage.language)
      setCommand += " Set " + predicateAndLanguage.getTokenPredicateString();
    else
      setCommand += " Set-Language " + predicateAndLanguage.getLanguageNormalizedName() + " " + predicateAndLanguage.getTokenPredicateString();

    return new PredicateEntry(setCommand, predicateAndLanguage.getTokenPredicateString());
  }

  @Override
  public Object accessField(String rawIdentifier) {
    return switch (rawIdentifier) {
      case "set_command" -> setCommand;
      case "predicate" -> predicate;
      default -> DirectFieldAccess.UNKNOWN_FIELD_SENTINEL;
    };
  }

  @Override
  public Set<String> getAvailableFields() {
    return Set.of("set_command", "predicate");
  }
}
