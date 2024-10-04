package me.blvckbytes.item_predicate_parser;

import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import me.blvckbytes.item_predicate_parser.config.MainSection;
import me.blvckbytes.item_predicate_parser.parse.EnumMatcher;
import me.blvckbytes.item_predicate_parser.parse.EnumPredicate;
import me.blvckbytes.item_predicate_parser.parse.ItemPredicateParseException;
import me.blvckbytes.item_predicate_parser.parse.NormalizedConstant;
import me.blvckbytes.item_predicate_parser.predicate.ItemPredicate;
import me.blvckbytes.item_predicate_parser.translation.TranslationLanguage;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ItemPredicateParserCommand implements CommandExecutor, TabCompleter {

  private enum CommandAction {
    // /ipp reload
    RELOAD,

    // /ipp test <language> <predicate>
    TEST
    ;

    static final EnumMatcher<CommandAction> matcher = new EnumMatcher<>(values());

    public static EnumPredicate<CommandAction> makeFilter(Player player) {
      return item -> (
        switch (item.constant) {
          case TEST -> PluginPermission.IPP_TEST_COMMAND.has(player);
          case RELOAD -> PluginPermission.IPP_RELOAD_COMMAND.has(player);
        }
      );
    }
  }

  public static final String COMMAND_NAME = "itempredicateparser";

  private final PredicateHelper predicateHelper;
  private final ConfigKeeper<MainSection> config;
  private final Logger logger;

  public ItemPredicateParserCommand(
    PredicateHelper predicateHelper,
    ConfigKeeper<MainSection> config,
    Logger logger
  ) {
    this.predicateHelper = predicateHelper;
    this.config = config;
    this.logger = logger;
  }

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    var actionFilter = makeFilter(sender);

    NormalizedConstant<CommandAction> action;

    if (args.length < 1 || (action = CommandAction.matcher.matchFirst(args[0], actionFilter)) == null) {
      var suggestions = CommandAction.matcher.createCompletions(null, actionFilter);

      if (suggestions.isEmpty())
        sender.sendMessage("§cYou have no access to this command");
      else
        sender.sendMessage("§cUsage: /" + label + " <" + String.join(", ", suggestions) + ">");

      return true;
    }

    switch (action.constant) {
      case TEST -> {
        if (!(sender instanceof Player player)) {
          sender.sendMessage("§cOnly available for players");
          return true;
        }

        if (!PluginPermission.IPP_TEST_COMMAND.has(player)) {
          player.sendMessage("§cMissing permission to execute test command");
          return true;
        }

        if (args.length < 2) {
          sender.sendMessage("§cUsage: /" + label + " " + action.normalizedName + " <language>");
          return true;
        }

        var language = TranslationLanguage.matcher.matchFirst(args[1]);

        if (language == null) {
          sender.sendMessage("§cUsage: /" + label + " " + action.normalizedName + " <" + String.join(", ", TranslationLanguage.matcher.createCompletions(null)) + ">");
          return true;
        }

        ItemPredicate predicate;

        try {
          var tokens = predicateHelper.parseTokens(args, 2);
          predicate = predicateHelper.parsePredicate(language.constant, tokens);
        } catch (ItemPredicateParseException e) {
          player.sendMessage(predicateHelper.createExceptionMessage(e));
          return true;
        }

        if (predicate == null) {
          player.sendMessage("§cPredicate empty");
          return true;
        }

        player.sendMessage("§7Token predicate: §a" + predicate.stringify(true));
        player.sendMessage("§7Expanded predicate: §a" + predicate.stringify(false));

        if (predicate.test(player.getInventory().getItemInMainHand())) {
          player.sendMessage("§7Result: §aThe item in your hand matches");
          return true;
        }

        player.sendMessage("§7Result: §cThe item in your hand mismatches");
        return true;
      }

      case RELOAD -> {
        if (sender instanceof Player player && !PluginPermission.IPP_RELOAD_COMMAND.has(player)) {
          player.sendMessage("§cMissing permission to execute reload command");
          return true;
        }

        try {
          config.reload();
          sender.sendMessage("§aConfig reloaded!");
        } catch (Exception e) {
          logger.log(Level.SEVERE, "An error occurred while trying to reload the config", e);
          sender.sendMessage("§cAn error occurred while trying to reload the config");
        }

        return true;
      }

      default -> { return true; }
    }
  }

  private @Nullable EnumPredicate<CommandAction> makeFilter(CommandSender sender) {
    return sender instanceof Player player ? CommandAction.makeFilter(player) : null;
  }

  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    var actionFilter = makeFilter(sender);

    if (args.length == 1)
      return CommandAction.matcher.createCompletions(args[0], actionFilter);

    var action = CommandAction.matcher.matchFirst(args[0], actionFilter);

    if (action == null)
      return List.of();

    if (action.constant == CommandAction.TEST) {
      if (!(sender instanceof Player player)) {
        sender.sendMessage("§cOnly available for players");
        return List.of();
      }

      if (!PluginPermission.IPP_TEST_COMMAND.has(player))
        return List.of();

      if (args.length == 2)
        return TranslationLanguage.matcher.createCompletions(args[1]);

      var language = TranslationLanguage.matcher.matchFirst(args[1]);

      if (language == null)
        return List.of();

      try {
        var tokens = predicateHelper.parseTokens(args, 2);
        var completion = predicateHelper.createCompletion(language.constant, tokens);

        if (completion.expandedPreviewOrError() != null)
          showActionBarMessage(player, completion.expandedPreviewOrError());

        return completion.suggestions();
      } catch (ItemPredicateParseException e) {
        showActionBarMessage(player, predicateHelper.createExceptionMessage(e));
        return List.of();
      }
    }

    return List.of();
  }

  private void showActionBarMessage(Player player, String message) {
    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
  }
}
