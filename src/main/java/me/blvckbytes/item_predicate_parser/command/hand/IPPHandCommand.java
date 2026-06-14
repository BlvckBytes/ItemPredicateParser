package me.blvckbytes.item_predicate_parser.command.hand;

import at.blvckbytes.cm_mapper.ConfigKeeper;
import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import me.blvckbytes.item_predicate_parser.PredicateHelper;
import me.blvckbytes.item_predicate_parser.command.PredicateEntry;
import me.blvckbytes.item_predicate_parser.command.main.IPPCommand;
import me.blvckbytes.item_predicate_parser.config.MainSection;
import me.blvckbytes.item_predicate_parser.event.PredicateHandGetEvent;
import me.blvckbytes.item_predicate_parser.event.PredicateHandRemoveEvent;
import me.blvckbytes.item_predicate_parser.event.PredicateHandSetEvent;
import me.blvckbytes.syllables_matcher.NormalizedConstant;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class IPPHandCommand implements CommandExecutor, TabCompleter {

  private final ConfigKeeper<MainSection> config;
  private final PredicateHelper predicateHelper;
  private final IPPCommand ippCommand;

  public IPPHandCommand(
    ConfigKeeper<MainSection> config,
    PredicateHelper predicateHelper,
    IPPCommand ippCommand
  ) {
    this.config = config;
    this.predicateHelper = predicateHelper;
    this.ippCommand = ippCommand;
  }

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
    if (!(sender instanceof Player player)) {
      config.rootSection.playerMessages.commandOnlyForPlayers.sendMessage(sender);
      return true;
    }

    var actionFilter = CommandAction.makeFilter(player);

    NormalizedConstant<CommandAction> action;

    if (args.length < 1 || (action = CommandAction.matcher.matchFirst(args[0], actionFilter)) == null) {
      var suggestions = CommandAction.matcher.createCompletions(null, actionFilter);

      if (suggestions.isEmpty()) {
        config.rootSection.playerMessages.missingPermissionIppCommand.sendMessage(player);
        return true;
      }

      config.rootSection.playerMessages.usageIppHandCommandAction.sendMessage(
        player,
        new InterpretationEnvironment()
          .withVariable("label", label)
          .withVariable("actions", suggestions)
      );

      return true;
    }

    var playerInventory = player.getInventory();
    var heldItem = playerInventory.getItemInMainHand();

    if (heldItem.getType().isAir()) {
      config.rootSection.playerMessages.ippHandNoItemHeld.sendMessage(player);
      return true;
    }

    var heldSlot = playerInventory.getHeldItemSlot();

    switch (action.constant) {
      case REMOVE, REMOVE_ALL -> {
        var all = action.constant == CommandAction.REMOVE_ALL;

        var removeEvent = new PredicateHandRemoveEvent(player, heldSlot, all);
        Bukkit.getPluginManager().callEvent(removeEvent);

        if (!removeEvent.isAcknowledged()) {
          config.rootSection.playerMessages.heldItemNotSupportPredicates.sendMessage(player);
          return true;
        }

        var removedPredicates = removeEvent.getRemovedPredicates();

        if (removedPredicates == null || removedPredicates.isEmpty()) {
          if (all) {
            config.rootSection.playerMessages.heldItemsHaveNoPredicatesSet.sendMessage(player);
            return true;
          }

          config.rootSection.playerMessages.heldItemHasNoPredicateSet.sendMessage(player);
          return true;
        }

        var removedEntries = new ArrayList<PredicateEntry>();

        predicateLoop:
        for (var index = 0; index < removedPredicates.size(); ++index) {
          var currentRemovedPredicate = removedPredicates.get(index);

          for (var subIndex = 0; subIndex < index; ++subIndex) {
            var priorRemovedPredicate = removedPredicates.get(subIndex);

            if (currentRemovedPredicate.predicate.equals(priorRemovedPredicate.predicate))
              continue predicateLoop;
          }

          removedEntries.add(PredicateEntry.fromPredicateAndLanguage(currentRemovedPredicate, player, label, predicateHelper));
        }

        if (all) {
          config.rootSection.playerMessages.removedPredicatesFromHeldItems.sendMessage(
            player,
            new InterpretationEnvironment()
              .withVariable("encountered_stacks", removeEvent.getEncounteredStacks())
              .withVariable("affected_stacks", removedPredicates.size())
              .withVariable("removed_predicates", removedEntries)
          );

          return true;
        }

        var entry = removedEntries.get(0);

        config.rootSection.playerMessages.removedPredicateFromHeldItem.sendMessage(
          player,
          new InterpretationEnvironment()
            .withVariable("predicate", entry.predicate())
            .withVariable("set_command", entry.setCommand())
        );

        return true;
      }

      case SET, SET_LANGUAGE, SET_ALL, SET_ALL_LANGUAGE -> {
        var predicateAndLanguage = ippCommand.tryParsePredicateAndLanguage(player, action.constant == CommandAction.SET_LANGUAGE || action.constant == CommandAction.SET_ALL_LANGUAGE, args);

        if (predicateAndLanguage == null)
          return true;

        var all = action.constant == CommandAction.SET_ALL || action.constant == CommandAction.SET_ALL_LANGUAGE;

        var setEvent = new PredicateHandSetEvent(player, heldSlot, all, predicateAndLanguage);
        Bukkit.getPluginManager().callEvent(setEvent);

        if (!setEvent.isAcknowledged()) {
          config.rootSection.playerMessages.heldItemNotSupportPredicates.sendMessage(player);
          return true;
        }

        var entry = PredicateEntry.fromPredicateAndLanguage(predicateAndLanguage, player, label, predicateHelper);

        var environment = new InterpretationEnvironment()
          .withVariable("predicate", entry.predicate())
          .withVariable("set_command", entry.setCommand());

        if (all) {
          config.rootSection.playerMessages.predicateSetOnHeldItems.sendMessage(
            player,
            environment
              .withVariable("encountered_stacks", setEvent.getEncounteredStacks())
          );

          return true;
        }

        config.rootSection.playerMessages.predicateSetOnHeldItem.sendMessage(player, environment);
        return true;
      }

      case GET -> {
        var getEvent = new PredicateHandGetEvent(player, heldSlot);
        Bukkit.getPluginManager().callEvent(getEvent);

        if (!getEvent.isAcknowledged()) {
          config.rootSection.playerMessages.heldItemNotSupportPredicates.sendMessage(player);
          return true;
        }

        var result = getEvent.getResult();

        if (result == null) {
          var error = getEvent.getError();

          if (error != null) {
            config.rootSection.playerMessages.heldItemHasPredicateError.sendMessage(
              player,
              new InterpretationEnvironment()
                .withVariable("predicate_error", predicateHelper.createExceptionMessage(error))
            );

            return true;
          }

          config.rootSection.playerMessages.heldItemHasNoPredicateSet.sendMessage(player);
          return true;
        }

        var entry = PredicateEntry.fromPredicateAndLanguage(result, player, label, predicateHelper);

        config.rootSection.playerMessages.getPredicateFromHeldItem.sendMessage(
          player,
          new InterpretationEnvironment()
            .withVariable("predicate", entry.predicate())
            .withVariable("set_command", entry.setCommand())
        );

        return true;
      }

      default -> { return true; }
    }
  }

  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
    if (!(sender instanceof Player player))
      return List.of();

    var actionFilter = CommandAction.makeFilter(player);

    if (args.length == 1)
      return CommandAction.matcher.createCompletions(args[0], actionFilter);

    var action = CommandAction.matcher.matchFirst(args[0], actionFilter);

    if (action == null)
      return List.of();

    if (action.constant == CommandAction.SET || action.constant == CommandAction.SET_LANGUAGE || action.constant == CommandAction.SET_ALL || action.constant == CommandAction.SET_ALL_LANGUAGE)
      return ippCommand.completePredicateArgs(player, action.constant == CommandAction.SET_LANGUAGE || action.constant == CommandAction.SET_ALL_LANGUAGE, args);

    return List.of();
  }
}
