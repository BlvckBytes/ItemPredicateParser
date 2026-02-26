package me.blvckbytes.item_predicate_parser.command;

import me.blvckbytes.item_predicate_parser.PluginPermission;
import me.blvckbytes.syllables_matcher.EnumMatcher;
import me.blvckbytes.syllables_matcher.EnumPredicate;
import me.blvckbytes.syllables_matcher.MatchableEnum;
import org.bukkit.entity.Player;

public enum CommandAction implements MatchableEnum {
  RELOAD,
  VARIABLES,
  LANGUAGE,
  TEST,
  GET,
  SET,
  SET_LANGUAGE,
  REMOVE,
  ;

  static final EnumMatcher<CommandAction> matcher = new EnumMatcher<>(values());

  public static EnumPredicate<CommandAction> makeFilter(Player player) {
    return item -> (
      switch (item.constant) {
        case TEST -> PluginPermission.IPP_TEST_COMMAND.has(player);
        case LANGUAGE -> PluginPermission.IPP_LANGUAGE_COMMAND.has(player);
        case VARIABLES -> PluginPermission.IPP_VARIABLES_COMMAND.has(player);
        case RELOAD -> PluginPermission.IPP_RELOAD_COMMAND.has(player);
        case GET -> PluginPermission.IPP_GET_COMMAND.has(player);
        case SET, SET_LANGUAGE -> PluginPermission.IPP_SET_COMMAND.has(player);
        case REMOVE -> PluginPermission.IPP_REMOVE_COMMAND.has(player);
      }
    );
  }
}
