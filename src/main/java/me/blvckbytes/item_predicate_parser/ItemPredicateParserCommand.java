package me.blvckbytes.item_predicate_parser;

import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import me.blvckbytes.bukkitevaluable.ReloadPriority;
import me.blvckbytes.item_predicate_parser.config.HighlightPredicateFunction;
import me.blvckbytes.item_predicate_parser.config.MainSection;
import me.blvckbytes.item_predicate_parser.display.overview.DisplayedVariable;
import me.blvckbytes.item_predicate_parser.display.overview.VariablesDisplayData;
import me.blvckbytes.item_predicate_parser.display.overview.VariablesDisplayHandler;
import me.blvckbytes.item_predicate_parser.parse.ItemPredicateParseException;
import me.blvckbytes.item_predicate_parser.predicate.ItemPredicate;
import me.blvckbytes.item_predicate_parser.predicate.PredicateState;
import me.blvckbytes.item_predicate_parser.predicate.StringifyState;
import me.blvckbytes.item_predicate_parser.translation.LanguageRegistry;
import me.blvckbytes.item_predicate_parser.translation.TranslatedLangKeyed;
import me.blvckbytes.item_predicate_parser.translation.TranslationLanguage;
import me.blvckbytes.item_predicate_parser.translation.keyed.VariableKey;
import me.blvckbytes.syllables_matcher.EnumMatcher;
import me.blvckbytes.syllables_matcher.EnumPredicate;
import me.blvckbytes.syllables_matcher.MatchableEnum;
import me.blvckbytes.syllables_matcher.NormalizedConstant;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ItemPredicateParserCommand implements CommandExecutor, TabCompleter {

  private enum CommandAction implements MatchableEnum {
    // /ipp reload
    RELOAD,

    // /ipp variables
    VARIABLES,

    // /ipp test <language> <predicate>
    TEST
    ;

    static final EnumMatcher<CommandAction> matcher = new EnumMatcher<>(values());

    public static EnumPredicate<CommandAction> makeFilter(Player player) {
      return item -> (
        switch (item.constant) {
          case TEST -> PluginPermission.IPP_TEST_COMMAND.has(player);
          case VARIABLES -> PluginPermission.IPP_VARIABLES_COMMAND.has(player);
          case RELOAD -> PluginPermission.IPP_RELOAD_COMMAND.has(player);
        }
      );
    }
  }

  private final VariablesDisplayHandler variablesDisplayHandler;
  private final LanguageRegistry languageRegistry;
  private final PredicateHelper predicateHelper;
  private final ConfigKeeper<MainSection> config;
  private final Logger logger;

  private final Map<TranslationLanguage, List<TranslatedLangKeyed<VariableKey>>> variablesByLanguage;

  public ItemPredicateParserCommand(
    VariablesDisplayHandler variablesDisplayHandler,
    LanguageRegistry languageRegistry,
    PredicateHelper predicateHelper,
    ConfigKeeper<MainSection> config,
    Logger logger
  ) {
    this.variablesDisplayHandler = variablesDisplayHandler;
    this.languageRegistry = languageRegistry;
    this.predicateHelper = predicateHelper;
    this.config = config;
    this.logger = logger;

    this.variablesByLanguage = new HashMap<>();

    config.registerReloadListener(this::findVariables, ReloadPriority.LOW);

    findVariables();
  }

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    var actionFilter = makeFilter(sender);

    BukkitEvaluable message;
    NormalizedConstant<CommandAction> action;

    if (args.length < 1 || (action = CommandAction.matcher.matchFirst(args[0], actionFilter)) == null) {
      var suggestions = CommandAction.matcher.createCompletions(null, actionFilter);

      if (suggestions.isEmpty()) {
        if ((message = config.rootSection.playerMessages.missingPermissionIppCommand) != null)
          message.sendMessage(sender, config.rootSection.builtBaseEnvironment);
        return true;
      }

      if ((message = config.rootSection.playerMessages.usageIppCommandAction) != null) {
        message.sendMessage(
          sender,
          config.rootSection.getBaseEnvironment()
            .withStaticVariable("label", label)
            .withStaticVariable("actions", suggestions)
            .build()
        );
      }

      return true;
    }

    switch (action.constant) {
      case TEST -> {
        if (!(sender instanceof Player player)) {
          if ((message = config.rootSection.playerMessages.commandOnlyForPlayers) != null)
            message.sendMessage(sender, config.rootSection.builtBaseEnvironment);
          return true;
        }

        if (!PluginPermission.IPP_TEST_COMMAND.has(player)) {
          if ((message = config.rootSection.playerMessages.missingPermissionIppTestCommand) != null)
            message.sendMessage(sender, config.rootSection.builtBaseEnvironment);
          return true;
        }

        NormalizedConstant<TranslationLanguage> language;

        if (args.length < 2 || (language = TranslationLanguage.matcher.matchFirst(args[1])) == null) {
          if ((message = config.rootSection.playerMessages.usageIppTestCommandLanguage) != null) {
            message.sendMessage(
              sender,
              config.rootSection.getBaseEnvironment()
                .withStaticVariable("label", label)
                .withStaticVariable("action", action.getNormalizedName())
                .withStaticVariable("languages", TranslationLanguage.matcher.createCompletions(null))
                .build()
            );
          }
          return true;
        }

        ItemPredicate predicate;

        try {
          var tokens = predicateHelper.parseTokens(args, 2);
          predicate = predicateHelper.parsePredicate(language.constant, tokens);
        } catch (ItemPredicateParseException e) {
          if ((message = config.rootSection.playerMessages.predicateParseError) != null) {
            message.sendMessage(
              sender,
              config.rootSection.getBaseEnvironment()
                .withStaticVariable("exception_message", predicateHelper.createExceptionMessage(e))
                .build()
            );
          }
          return true;
        }

        if (predicate == null) {
          if ((message = config.rootSection.playerMessages.emptyPredicate) != null)
            message.sendMessage(sender, config.rootSection.builtBaseEnvironment);
          return true;
        }

        var failure = predicate.testForFailure(new PredicateState(player.getInventory().getItemInMainHand()));

        if ((message = config.rootSection.playerMessages.predicateTestResult) != null) {
          message.sendMessage(
            player,
            config.rootSection.getBaseEnvironment()
              .withStaticVariable("entered_predicate",
                new StringifyState(true).appendPredicate(predicate).toString()
              )
              .withFunction("expanded_predicate", new HighlightPredicateFunction(predicate, failure))
              .build()
          );
        }

        return true;
      }

      case RELOAD -> {
        if (sender instanceof Player player && !PluginPermission.IPP_RELOAD_COMMAND.has(player)) {
          if ((message = config.rootSection.playerMessages.missingPermissionIppReloadCommand) != null)
            message.sendMessage(sender, config.rootSection.builtBaseEnvironment);
          return true;
        }

        try {
          config.reload();

          if ((message = config.rootSection.playerMessages.pluginReloadedSuccess) != null)
            message.sendMessage(sender, config.rootSection.builtBaseEnvironment);
        } catch (Exception e) {
          logger.log(Level.SEVERE, "An error occurred while trying to reload the config", e);

          if ((message = config.rootSection.playerMessages.pluginReloadedError) != null)
            message.sendMessage(sender, config.rootSection.builtBaseEnvironment);
        }

        return true;
      }

      case VARIABLES -> {
        if (!(sender instanceof Player player)) {
          if ((message = config.rootSection.playerMessages.commandOnlyForPlayers) != null)
            message.sendMessage(sender, config.rootSection.builtBaseEnvironment);
          return true;
        }

        if (!PluginPermission.IPP_VARIABLES_COMMAND.has(player)) {
          if ((message = config.rootSection.playerMessages.missingPermissionIppVariablesCommand) != null)
            message.sendMessage(sender, config.rootSection.builtBaseEnvironment);
          return true;
        }

        NormalizedConstant<TranslationLanguage> language;

        if (args.length < 2 || (language = TranslationLanguage.matcher.matchFirst(args[1])) == null) {
          if ((message = config.rootSection.playerMessages.usageIppVariablesCommandLanguage) != null) {
            message.sendMessage(
              sender,
              config.rootSection.getBaseEnvironment()
                .withStaticVariable("label", label)
                .withStaticVariable("action", action.getNormalizedName())
                .withStaticVariable("languages", TranslationLanguage.matcher.createCompletions(null))
                .build()
            );
          }
          return true;
        }

        var targetName = args.length > 2 ? args[2] : null;
        var variables = new ArrayList<DisplayedVariable>();
        var translationRegistry = languageRegistry.getTranslationRegistry(language.constant);

        for (var item : variablesByLanguage.getOrDefault(language.constant, Collections.emptyList())) {
          if (targetName != null && !item.normalizedUnPrefixedTranslation.equals(targetName))
            continue;

          var materialDisplayNames = new ArrayList<String>();

          for (var material : item.langKeyed.getWrapped().materials) {
            var translation = translationRegistry.getTranslationBySingleton(material);

            if (translation == null)
              translation = "<null>";

            materialDisplayNames.add(translation);
          }

          variables.add(new DisplayedVariable(item.langKeyed.getWrapped().icon, item.normalizedUnPrefixedTranslation, materialDisplayNames));
        }

        if (targetName != null && variables.isEmpty()) {
          if ((message = config.rootSection.playerMessages.unknownVariableName) != null) {
            message.sendMessage(
              player,
              config.rootSection.getBaseEnvironment()
                .withStaticVariable("name", targetName)
                .build()
            );
          }

          return true;
        }

        if ((message = config.rootSection.playerMessages.showingVariables) != null) {
          message.sendMessage(
            player,
            config.rootSection.getBaseEnvironment()
              .withStaticVariable("count", variables.size())
              .build()
          );
        }

        variablesDisplayHandler.show(player, new VariablesDisplayData(variables));
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

    if (!(sender instanceof Player player)) {
      BukkitEvaluable message;

      if ((message = config.rootSection.playerMessages.commandOnlyForPlayers) != null)
        message.sendMessage(sender, config.rootSection.builtBaseEnvironment);

      return List.of();
    }

    if (action.constant == CommandAction.VARIABLES) {
      if (!PluginPermission.IPP_VARIABLES_COMMAND.has(player))
        return List.of();

      if (args.length == 2)
        return TranslationLanguage.matcher.createCompletions(args[1]);

      var language = TranslationLanguage.matcher.matchFirst(args[1]);

      if (language == null)
        return List.of();

      if (args.length == 3) {
        return variablesByLanguage.get(language.constant)
          .stream()
          .map(it -> it.normalizedUnPrefixedTranslation)
          .filter(it -> StringUtils.startsWithIgnoreCase(it, args[2]))
          .toList();
      }

      return List.of();
    }

    if (action.constant == CommandAction.TEST) {
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

  private void findVariables() {
    this.variablesByLanguage.clear();
    for (var language : TranslationLanguage.values()) {
      var translationRegistry = languageRegistry.getTranslationRegistry(language);
      var variables = translationRegistry.lookup(VariableKey.class);
      this.variablesByLanguage.put(language, variables);
    }
  }
}
