package me.blvckbytes.item_predicate_parser.command;

import at.blvckbytes.cm_mapper.ConfigKeeper;
import at.blvckbytes.cm_mapper.ReloadPriority;
import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import me.blvckbytes.item_predicate_parser.NameScopedKeyValueStore;
import me.blvckbytes.item_predicate_parser.PredicateHelper;
import me.blvckbytes.item_predicate_parser.config.MainSection;
import me.blvckbytes.item_predicate_parser.display.overview.DisplayedVariable;
import me.blvckbytes.item_predicate_parser.display.overview.VariablesDisplayData;
import me.blvckbytes.item_predicate_parser.display.overview.VariablesDisplayHandler;
import me.blvckbytes.item_predicate_parser.parse.ItemPredicateParseException;
import me.blvckbytes.item_predicate_parser.predicate.stringify.TokenHighlighter;
import me.blvckbytes.item_predicate_parser.predicate.ItemPredicate;
import me.blvckbytes.item_predicate_parser.predicate.PredicateState;
import me.blvckbytes.item_predicate_parser.predicate.VariablePredicate;
import me.blvckbytes.item_predicate_parser.translation.LanguageRegistry;
import me.blvckbytes.item_predicate_parser.translation.TranslatedLangKeyed;
import me.blvckbytes.item_predicate_parser.translation.TranslationLanguage;
import me.blvckbytes.item_predicate_parser.translation.keyed.VariableKey;
import me.blvckbytes.syllables_matcher.*;
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

    NormalizedConstant<CommandAction> action;

    if (args.length < 1 || (action = CommandAction.matcher.matchFirst(args[0], actionFilter)) == null) {
      var suggestions = CommandAction.matcher.createCompletions(null, actionFilter);

      if (suggestions.isEmpty()) {
        config.rootSection.playerMessages.missingPermissionIppCommand.sendMessage(sender);
        return true;
      }

      config.rootSection.playerMessages.usageIppCommandAction.sendMessage(
        sender,
        new InterpretationEnvironment()
          .withVariable("label", label)
          .withVariable("actions", suggestions)
      );

      return true;
    }

    switch (action.constant) {
      case TEST -> {
        if (!(sender instanceof Player player)) {
          config.rootSection.playerMessages.commandOnlyForPlayers.sendMessage(sender);
          return true;
        }

        var language = predicateHelper.getSelectedLanguage(player);

        ItemPredicate predicate;

        try {
          var tokens = predicateHelper.parseTokens(args, 1);
          predicate = predicateHelper.parsePredicate(language, tokens);
        } catch (ItemPredicateParseException e) {
          config.rootSection.playerMessages.predicateParseError.sendMessage(
            sender,
            new InterpretationEnvironment()
              .withVariable("exception", predicateHelper.createExceptionMessage(e))
          );
          return true;
        }

        var itemInHand = player.getInventory().getItemInMainHand();

        if (itemInHand.getType().isAir()) {
          config.rootSection.playerMessages.noItemInMainHand.sendMessage(player);
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
            config.rootSection.playerMessages.variablesTestNoResults.sendMessage(player);
            return true;
          }

          config.rootSection.playerMessages.variablesTestResults.sendMessage(
            player,
            new InterpretationEnvironment()
              .withVariable("count", matchingNames.size())
              .withVariable("names", matchingNames)
          );

          return true;
        }

        var failedPredicate = predicate.testForFailure(new PredicateState(itemInHand));

        config.rootSection.playerMessages.predicateTestResult.sendMessage(
          player,
          new InterpretationEnvironment()
            .withVariable("predicate", TokenHighlighter.highlightFailure(config, predicate, failedPredicate))
        );

        return true;
      }

      case LANGUAGE -> {
        if (!(sender instanceof Player player)) {
          config.rootSection.playerMessages.commandOnlyForPlayers.sendMessage(sender);
          return true;
        }

        NormalizedConstant<TranslationLanguage> language;

        if (args.length < 2 || (language = TranslationLanguage.matcher.matchFirst(args[1])) == null) {
          config.rootSection.playerMessages.usageIppLanguage.sendMessage(
            sender,
            new InterpretationEnvironment()
              .withVariable("label", label)
              .withVariable("action", action.getNormalizedName())
              .withVariable("languages", TranslationLanguage.matcher.createCompletions(null))
          );
          return true;
        }

        keyValueStore.write(player.getUniqueId().toString(), NameScopedKeyValueStore.KEY_LANGUAGE, language.constant.name());

        config.rootSection.playerMessages.languageSelected.sendMessage(
          player,
          new InterpretationEnvironment()
            .withVariable("language", language.getNormalizedName())
        );

        return true;
      }

      case RELOAD -> {
        try {
          config.reload();
          config.rootSection.playerMessages.pluginReloadedSuccess.sendMessage(sender);
        } catch (Exception e) {
          logger.log(Level.SEVERE, "An error occurred while trying to reload the config", e);
          config.rootSection.playerMessages.pluginReloadedError.sendMessage(sender);
        }

        return true;
      }

      case VARIABLES -> {
        if (!(sender instanceof Player player)) {
          config.rootSection.playerMessages.commandOnlyForPlayers.sendMessage(sender);
          return true;
        }

        var language = predicateHelper.getSelectedLanguage(player);
        var targetName = args.length > 1 ? args[1] : null;
        var variables = new ArrayList<DisplayedVariable>();
        var translationRegistry = languageRegistry.getTranslationRegistry(language);

        for (var item : variablesByLanguage.getOrDefault(language, Collections.emptyList())) {
          if (targetName != null && !item.normalizedUnPrefixedTranslation.equals(targetName))
            continue;

          var variable = item.langKeyed.getWrapped();

          var materialDisplayNames = new ArrayList<String>();
          variable.forEachMaterialName(translationRegistry, materialDisplayNames::add);

          var blockedMaterialDisplayNames = new ArrayList<String>();
          variable.forEachBlockedMaterialName(translationRegistry, blockedMaterialDisplayNames::add);

          var inheritedMaterialDisplayNames = new ArrayList<String>();
          variable.forEachInheritedMaterialName(translationRegistry, inheritedMaterialDisplayNames::add);

          var parentDisplayNames = new ArrayList<String>();

          for (var parent : variable.parents)
            parentDisplayNames.add(parent.getFinalName(language));

          variables.add(new DisplayedVariable(
            variable.icon,
            item.normalizedUnPrefixedTranslation,
            materialDisplayNames,
            blockedMaterialDisplayNames,
            parentDisplayNames,
            inheritedMaterialDisplayNames
          ));
        }

        if (targetName != null && variables.isEmpty()) {
          config.rootSection.playerMessages.unknownVariableName.sendMessage(
            player,
            new InterpretationEnvironment()
              .withVariable("name", targetName)
          );

          return true;
        }

        config.rootSection.playerMessages.showingVariables.sendMessage(
          player,
          new InterpretationEnvironment()
            .withVariable("count", variables.size())
        );

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
      config.rootSection.playerMessages.commandOnlyForPlayers.sendMessage(sender);
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
        var result = new ArrayList<String>();
        var syllablesMatcher = new SyllablesMatcher();

        syllablesMatcher.setQuery(Syllables.forString(args[1], Syllables.DELIMITER_SEARCH_PATTERN));

        for (var variable : variablesByLanguage.get(language)) {
          syllablesMatcher.resetQueryMatches();
          syllablesMatcher.setTarget(variable.syllables);
          syllablesMatcher.match();

          if (syllablesMatcher.hasUnmatchedQuerySyllables())
            continue;

          result.add(variable.normalizedUnPrefixedTranslation);
        }

        return result;
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
