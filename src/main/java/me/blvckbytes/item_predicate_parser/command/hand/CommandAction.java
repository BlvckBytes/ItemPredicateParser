package me.blvckbytes.item_predicate_parser.command.hand;

import me.blvckbytes.item_predicate_parser.PluginPermission;
import me.blvckbytes.syllables_matcher.EnumMatcher;
import me.blvckbytes.syllables_matcher.EnumPredicate;
import me.blvckbytes.syllables_matcher.MatchableEnum;
import org.bukkit.entity.Player;

public enum CommandAction implements MatchableEnum {
  GET,
  SET,
  SET_LANGUAGE,
  SET_ALL,
  SET_ALL_LANGUAGE,
  REMOVE,
  REMOVE_ALL,
  ;

  static final EnumMatcher<CommandAction> matcher = new EnumMatcher<>(values());

  public static EnumPredicate<CommandAction> makeFilter(Player player) {
    return item -> (
      switch (item.constant) {
        case GET -> PluginPermission.IPP_GET_COMMAND.has(player);
        case SET, SET_ALL, SET_LANGUAGE, SET_ALL_LANGUAGE -> PluginPermission.IPP_SET_COMMAND.has(player);
        case REMOVE, REMOVE_ALL -> PluginPermission.IPP_REMOVE_COMMAND.has(player);
      }
    );
  }
}
