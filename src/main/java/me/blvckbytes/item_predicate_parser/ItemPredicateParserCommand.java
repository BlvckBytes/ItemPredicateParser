package me.blvckbytes.item_predicate_parser;

import at.blvckbytes.cm_mapper.ConfigKeeper;
import at.blvckbytes.cm_mapper.ReloadPriority;
import at.blvckbytes.cm_mapper.cm.ComponentMarkup;
import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import me.blvckbytes.item_predicate_parser.config.MainSection;
import me.blvckbytes.item_predicate_parser.display.overview.DisplayedVariable;
import me.blvckbytes.item_predicate_parser.display.overview.VariablesDisplayData;
import me.blvckbytes.item_predicate_parser.display.overview.VariablesDisplayHandler;
import me.blvckbytes.item_predicate_parser.parse.ItemPredicateParseException;
import me.blvckbytes.item_predicate_parser.predicate.ItemPredicate;
import me.blvckbytes.item_predicate_parser.predicate.PredicateState;
import me.blvckbytes.item_predicate_parser.predicate.StringifyState;
import me.blvckbytes.item_predicate_parser.predicate.VariablePredicate;
import me.blvckbytes.item_predicate_parser.translation.LanguageRegistry;
import me.blvckbytes.item_predicate_parser.translation.TranslatedLangKeyed;
import me.blvckbytes.item_predicate_parser.translation.TranslationLanguage;
import me.blvckbytes.item_predicate_parser.translation.keyed.VariableKey;
import me.blvckbytes.syllables_matcher.EnumMatcher;
import me.blvckbytes.syllables_matcher.EnumPredicate;
import me.blvckbytes.syllables_matcher.MatchableEnum;
import me.blvckbytes.syllables_matcher.NormalizedConstant;
import org.apache.commons.lang3.StringUtils;
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
    RELOAD,
    VARIABLES,
    LANGUAGE,
    TEST,
    ;

    static final EnumMatcher<CommandAction> matcher = new EnumMatcher<>(values());

    public static EnumPredicate<CommandAction> makeFilter(Player player) {
      return item -> (
        switch (item.constant) {
          case TEST -> PluginPermission.IPP_TEST_COMMAND.has(player);
          case LANGUAGE -> PluginPermission.IPP_LANGUAGE_COMMAND.has(player);
          case VARIABLES -> PluginPermission.IPP_VARIABLES_COMMAND.has(player);
          case RELOAD -> PluginPermission.IPP_RELOAD_COMMAND.has(player);
        }
      );
    }
  }

  private final VariablesDisplayHandler variablesDisplayHandler;
  private final LanguageRegistry languageRegistry;
  private final NameScopedKeyValueStore keyValueStore;
  private final PredicateHelper predicateHelper;
  private final ConfigKeeper<MainSection> config;
  private final Logger logger;

  private final Map<TranslationLanguage, List<TranslatedLangKeyed<VariableKey>>> variablesByLanguage;

  public ItemPredicateParserCommand(
    VariablesDisplayHandler variablesDisplayHandler,
    LanguageRegistry languageRegistry,
    NameScopedKeyValueStore keyValueStore,
    PredicateHelper predicateHelper,
    ConfigKeeper<MainSection> config,
    Logger logger
  ) {
    this.variablesDisplayHandler = variablesDisplayHandler;
    this.languageRegistry = languageRegistry;
    this.keyValueStore = keyValueStore;
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

    ComponentMarkup message;
    NormalizedConstant<CommandAction> action;

    if (args.length < 1 || (action = CommandAction.matcher.matchFirst(args[0], actionFilter)) == null) {
      var suggestions = CommandAction.matcher.createCompletions(null, actionFilter);

      if (suggestions.isEmpty()) {
        if ((message = config.rootSection.playerMessages.missingPermissionIppCommand) != null)
          message.sendMessage(sender);
        return true;
      }

      if ((message = config.rootSection.playerMessages.usageIppCommandAction) != null) {
        message.sendMessage(
          sender,
          new InterpretationEnvironment()
            .withVariable("label", label)
            .withVariable("actions", suggestions)
        );
      }

      return true;
    }

    switch (action.constant) {
      case TEST -> {
        if (!(sender instanceof Player player)) {
          if ((message = config.rootSection.playerMessages.commandOnlyForPlayers) != null)
            message.sendMessage(sender);
          return true;
        }

        var language = predicateHelper.getSelectedLanguage(player);

        ItemPredicate predicate;

        try {
          var tokens = predicateHelper.parseTokens(args, 1);
          predicate = predicateHelper.parsePredicate(language, tokens);
        } catch (ItemPredicateParseException e) {
          if ((message = config.rootSection.playerMessages.predicateParseError) != null) {
            message.sendMessage(
              sender,
              new InterpretationEnvironment()
                .withVariable("exception_message", predicateHelper.createExceptionMessage(e))
            );
          }
          return true;
        }

        var itemInHand = player.getInventory().getItemInMainHand();

        if (itemInHand.getType().isAir()) {
          if ((message = config.rootSection.playerMessages.noItemInMainHand) != null)
            message.sendMessage(player);
          return true;
        }

        if (predicate == null) {
          var variables = variablesByLanguage.getOrDefault(language, Collections.emptyList());
          var matchingNames = new ArrayList<String>();

          for (var variable : variables) {
            if (new VariablePredicate(null, variable).test(itemInHand))
              matchingNames.add(variable.normalizedUnPrefixedTranslation);
          }

          if (matchingNames.isEmpty()) {
            if ((message = config.rootSection.playerMessages.variablesTestNoResults) != null)
              message.sendMessage(player);
            return true;
          }

          if ((message = config.rootSection.playerMessages.variablesTestResults) != null) {
            message.sendMessage(
              player,
              new InterpretationEnvironment()
                .withVariable("count", matchingNames.size())
                .withVariable("names", matchingNames)
            );
          }

          return true;
        }

        // TODO: Migrate this to the new way of ComponentMarkup

        var failure = predicate.testForFailure(new PredicateState(itemInHand));
        if ((message = config.rootSection.playerMessages.predicateTestResult) != null) {
          message.sendMessage(
            player,
            new InterpretationEnvironment()
              .withVariable("entered_predicate",
                new StringifyState(true).appendPredicate(predicate).toString()
              )
//              .withFunction("expanded_predicate", new HighlightPredicateFunction(predicate, failure))
          );
        }

        return true;
      }

      case LANGUAGE -> {
        if (!(sender instanceof Player player)) {
          if ((message = config.rootSection.playerMessages.commandOnlyForPlayers) != null)
            message.sendMessage(sender);
          return true;
        }

        NormalizedConstant<TranslationLanguage> language;

        if (args.length < 2 || (language = TranslationLanguage.matcher.matchFirst(args[1])) == null) {
          if ((message = config.rootSection.playerMessages.usageIppLanguageCommandLanguage) != null) {
            message.sendMessage(
              sender,
              new InterpretationEnvironment()
                .withVariable("label", label)
                .withVariable("action", action.getNormalizedName())
                .withVariable("languages", TranslationLanguage.matcher.createCompletions(null))
            );
          }
          return true;
        }

        keyValueStore.write(player.getUniqueId().toString(), NameScopedKeyValueStore.KEY_LANGUAGE, language.constant.name());

        if ((message = config.rootSection.playerMessages.languageSelected) != null) {
          message.sendMessage(
            player,
            new InterpretationEnvironment()
              .withVariable("language", language.getNormalizedName())
          );
        }

        return true;
      }

      case RELOAD -> {
        try {
          config.reload();

          if ((message = config.rootSection.playerMessages.pluginReloadedSuccess) != null)
            message.sendMessage(sender);
        } catch (Exception e) {
          logger.log(Level.SEVERE, "An error occurred while trying to reload the config", e);

          if ((message = config.rootSection.playerMessages.pluginReloadedError) != null)
            message.sendMessage(sender);
        }

        return true;
      }

      case VARIABLES -> {
        if (!(sender instanceof Player player)) {
          if ((message = config.rootSection.playerMessages.commandOnlyForPlayers) != null)
            message.sendMessage(sender);
          return true;
        }

        var language = predicateHelper.getSelectedLanguage(player);
        var targetName = args.length > 1 ? args[1] : null;
        var variables = new ArrayList<DisplayedVariable>();
        var translationRegistry = languageRegistry.getTranslationRegistry(language);

        for (var item : variablesByLanguage.getOrDefault(language, Collections.emptyList())) {
          if (targetName != null && !item.normalizedUnPrefixedTranslation.equals(targetName))
            continue;

          var materialDisplayNames = new ArrayList<String>();
          var inheritedMaterialDisplayNames = new ArrayList<String>();

          var variable = item.langKeyed.getWrapped();

          for (var material : variable.materials) {
            var translation = translationRegistry.getTranslationBySingleton(material);

            if (translation == null)
              translation = "<null>";

            materialDisplayNames.add(translation);
          }

          for (var material : variable.getInheritedMaterials()) {
            var translation = translationRegistry.getTranslationBySingleton(material);

            if (translation == null)
              translation = "<null>";

            inheritedMaterialDisplayNames.add(translation);
          }

          var parentDisplayNames = new ArrayList<String>();

          for (var parent : variable.parents)
            parentDisplayNames.add(parent.getFinalName(language));

          variables.add(new DisplayedVariable(
            variable.icon,
            item.normalizedUnPrefixedTranslation,
            materialDisplayNames,
            parentDisplayNames,
            inheritedMaterialDisplayNames
          ));
        }

        if (targetName != null && variables.isEmpty()) {
          if ((message = config.rootSection.playerMessages.unknownVariableName) != null) {
            message.sendMessage(
              player,
              new InterpretationEnvironment()
                .withVariable("name", targetName)
            );
          }

          return true;
        }

        if ((message = config.rootSection.playerMessages.showingVariables) != null) {
          message.sendMessage(
            player,
            new InterpretationEnvironment()
              .withVariable("count", variables.size())
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
      ComponentMarkup message;

      if ((message = config.rootSection.playerMessages.commandOnlyForPlayers) != null)
        message.sendMessage(sender);

      return List.of();
    }

    if (action.constant == CommandAction.LANGUAGE) {
      if (args.length == 2)
        return TranslationLanguage.matcher.createCompletions(args[1]);

      return List.of();
    }

    if (action.constant == CommandAction.VARIABLES) {
      var language = predicateHelper.getSelectedLanguage(player);

      if (args.length == 2) {
        return variablesByLanguage.get(language)
          .stream()
          .map(it -> it.normalizedUnPrefixedTranslation)
          .filter(it -> StringUtils.startsWithIgnoreCase(it, args[1]))
          .toList();
      }

      return List.of();
    }

    if (action.constant == CommandAction.TEST) {
      var language = predicateHelper.getSelectedLanguage(player);

      try {
        var tokens = predicateHelper.parseTokens(args, 1);
        var completion = predicateHelper.createCompletion(language, tokens);

        if (completion.expandedPreviewOrError() != null)
          player.sendActionBar(completion.expandedPreviewOrError());

        return completion.suggestions();
      } catch (ItemPredicateParseException e) {
        player.sendActionBar(predicateHelper.createExceptionMessage(e));
        return List.of();
      }
    }

    return List.of();
  }

  private void findVariables() {
    this.variablesByLanguage.clear();
    for (var language : TranslationLanguage.values()) {
      var translationRegistry = languageRegistry.getTranslationRegistry(language);
      var variables = translationRegistry.lookup(VariableKey.class);
      variables.sort(Comparator.comparing(it -> it.langKeyed.getWrapped().defaultName));
      this.variablesByLanguage.put(language, variables);
    }
  }
}
