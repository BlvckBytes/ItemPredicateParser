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
import me.blvckbytes.item_predicate_parser.event.PredicateAndLanguage;
import me.blvckbytes.item_predicate_parser.event.PredicateGetEvent;
import me.blvckbytes.item_predicate_parser.event.PredicateRemoveEvent;
import me.blvckbytes.item_predicate_parser.event.PredicateSetEvent;
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
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ItemPredicateParserCommand implements CommandExecutor, TabCompleter, Listener {

  private final VariablesDisplayHandler variablesDisplayHandler;
  private final LanguageRegistry languageRegistry;
  private final NameScopedKeyValueStore keyValueStore;
  private final PredicateHelper predicateHelper;
  private final ConfigKeeper<MainSection> config;
  private final Logger logger;

  private final Map<TranslationLanguage, List<TranslatedLangKeyed<VariableKey>>> variablesByLanguage;
  private final Map<UUID, PredicateInteractionSession> interactionSessionByPlayerId;

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
    this.interactionSessionByPlayerId = new HashMap<>();

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

      case GET -> {
        if (!(sender instanceof Player player)) {
          config.rootSection.playerMessages.commandOnlyForPlayers.sendMessage(sender);
          return true;
        }

        config.rootSection.playerMessages.getPredicateInitialized.sendMessage(player);

        interactionSessionByPlayerId.put(player.getUniqueId(), new PredicateInteractionSession(player, block -> {
          var getEvent = new PredicateGetEvent(player, block);
          Bukkit.getServer().getPluginManager().callEvent(getEvent);

          if (!getEvent.isAcknowledged()) {
            config.rootSection.playerMessages.blockDoesNotSupportPredicates.sendMessage(player, makeEnvironment(player, block, null));
            return;
          }

          if (getEvent.getDeniedAccessBlock() != null) {
            config.rootSection.playerMessages.accessDeniedForBlock.sendMessage(player, makeEnvironment(player, getEvent.getDeniedAccessBlock(), null));
            return;
          }

          var result = getEvent.getResult();
          var actualBlock = getEvent.getBlock();

          if (getEvent.getDataHoldingBlock() != null)
            actualBlock = getEvent.getDataHoldingBlock();

          if (result == null) {
            var error = getEvent.getError();

            if (error != null) {
              config.rootSection.playerMessages.blockHasPredicateError.sendMessage(
                player,
                makeEnvironment(player, actualBlock, null)
                  .withVariable("predicate_error", predicateHelper.createExceptionMessage(error))
              );

              return;
            }

            config.rootSection.playerMessages.blockHasNoPredicateSet.sendMessage(player, makeEnvironment(player, actualBlock, null));
            return;
          }

          config.rootSection.playerMessages.getPredicateFromBlock.sendMessage(player, makeEnvironment(player, actualBlock, result));
        }));

        return true;
      }

      case SET, SET_LANGUAGE -> {
        if (!(sender instanceof Player player)) {
          config.rootSection.playerMessages.commandOnlyForPlayers.sendMessage(sender);
          return true;
        }

        int predicateArgsOffset;
        TranslationLanguage language;

        if (action.constant == CommandAction.SET) {
          language = predicateHelper.getSelectedLanguage(player);
          predicateArgsOffset = 1;
        }

        else {
          if (args.length < 2) {
            config.rootSection.playerMessages.setPredicateMissingLanguage.sendMessage(player);
            return true;
          }

          var languageSelection = TranslationLanguage.matcher.matchFirst(args[1]);

          if (languageSelection == null) {
            config.rootSection.playerMessages.setPredicateUnknownLanguage.sendMessage(
              player,
              new InterpretationEnvironment()
                .withVariable("input", args[1])
                .withVariable("languages", TranslationLanguage.matcher.createCompletions(null))
            );
            return true;
          }

          language = languageSelection.constant;
          predicateArgsOffset = 2;
        }

        ItemPredicate predicate;

        try {
          var tokens = predicateHelper.parseTokens(args, predicateArgsOffset);
          predicate = predicateHelper.parsePredicate(language, tokens);
        } catch (ItemPredicateParseException e) {
          config.rootSection.playerMessages.predicateParseError.sendMessage(
            sender,
            new InterpretationEnvironment()
              .withVariable("exception", predicateHelper.createExceptionMessage(e))
          );

          return true;
        }

        if (predicate == null) {
          config.rootSection.playerMessages.setPredicateEmpty.sendMessage(player);
          return true;
        }

        var predicateAndLanguage = new PredicateAndLanguage(predicate, language);

        config.rootSection.playerMessages.setPredicateInitialized.sendMessage(player, makeEnvironment(player, null, predicateAndLanguage));

        interactionSessionByPlayerId.put(player.getUniqueId(), new PredicateInteractionSession(player, block -> {
          var setEvent = new PredicateSetEvent(player, block, predicateAndLanguage);
          Bukkit.getServer().getPluginManager().callEvent(setEvent);

          if (!setEvent.isAcknowledged()) {
            config.rootSection.playerMessages.blockDoesNotSupportPredicates.sendMessage(player, makeEnvironment(player, block, null));
            return;
          }

          if (setEvent.getDeniedAccessBlock() != null) {
            config.rootSection.playerMessages.accessDeniedForBlock.sendMessage(player, makeEnvironment(player, setEvent.getDeniedAccessBlock(), null));
            return;
          }

          var actualBlock = setEvent.getBlock();

          if (setEvent.getDataHoldingBlock() != null)
            actualBlock = setEvent.getDataHoldingBlock();

          config.rootSection.playerMessages.setPredicateOnBlock.sendMessage(player, makeEnvironment(player, actualBlock, predicateAndLanguage));
        }));

        return true;
      }

      case REMOVE -> {
        if (!(sender instanceof Player player)) {
          config.rootSection.playerMessages.commandOnlyForPlayers.sendMessage(sender);
          return true;
        }

        config.rootSection.playerMessages.removePredicateInitialized.sendMessage(player);

        interactionSessionByPlayerId.put(player.getUniqueId(), new PredicateInteractionSession(player, block -> {
          var removeEvent = new PredicateRemoveEvent(player, block);
          Bukkit.getServer().getPluginManager().callEvent(removeEvent);

          if (!removeEvent.isAcknowledged()) {
            config.rootSection.playerMessages.blockDoesNotSupportPredicates.sendMessage(player, makeEnvironment(player, block, null));
            return;
          }

          if (removeEvent.getDeniedAccessBlock() != null) {
            config.rootSection.playerMessages.accessDeniedForBlock.sendMessage(player, makeEnvironment(player, removeEvent.getDeniedAccessBlock(), null));
            return;
          }

          var actualBlock = removeEvent.getBlock();

          if (removeEvent.getDataHoldingBlock() != null)
            actualBlock = removeEvent.getDataHoldingBlock();

          if (removeEvent.getRemovedPredicate() == null) {
            config.rootSection.playerMessages.blockHasNoPredicateSet.sendMessage(player, makeEnvironment(player, actualBlock, null));
            return;
          }

          config.rootSection.playerMessages.removePredicateFromBlock.sendMessage(player, makeEnvironment(player, actualBlock, removeEvent.getRemovedPredicate()));
        }));

        return true;
      }

      default -> { return true; }
    }
  }

  private InterpretationEnvironment makeEnvironment(Player player, @Nullable Block block, @Nullable PredicateAndLanguage predicateAndLanguage) {
    var environment = new InterpretationEnvironment();

    if (block != null) {
      environment
        .withVariable("x", block.getX())
        .withVariable("y", block.getY())
        .withVariable("z", block.getZ());
    }

    if (predicateAndLanguage != null) {
      var setCommand = "/" + config.rootSection.commands.itemPredicateParser.getShortestNameOrAlias() + " ";

      if (predicateHelper.getSelectedLanguage(player) == predicateAndLanguage.language)
        setCommand += CommandAction.matcher.getNormalizedName(CommandAction.SET) + " " + predicateAndLanguage.getTokenPredicateString();
      else
        setCommand += CommandAction.matcher.getNormalizedName(CommandAction.SET_LANGUAGE) + " " + predicateAndLanguage.getLanguageNormalizedName() + " " + predicateAndLanguage.getTokenPredicateString();

      environment
        .withVariable("predicate", predicateAndLanguage.getTokenPredicateString())
        .withVariable("set_command", setCommand);
    }

    return environment;
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

    if (action.constant == CommandAction.SET || action.constant == CommandAction.SET_LANGUAGE || action.constant == CommandAction.TEST) {
      int argsOffset;
      TranslationLanguage language;

      if (action.constant == CommandAction.SET_LANGUAGE) {
        if (args.length == 2)
          return TranslationLanguage.matcher.createCompletions(args[1]);

        argsOffset = 2;

        var matchedLanguage = TranslationLanguage.matcher.matchFirst(args[1]);

        if (matchedLanguage == null)
          return List.of();

        language = matchedLanguage.constant;
      }

      else {
        argsOffset = 1;
        language = predicateHelper.getSelectedLanguage(player);
      }

      try {
        var tokens = predicateHelper.parseTokens(args, argsOffset);
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

  public void tickSessions() {
    for (var iterator = interactionSessionByPlayerId.values().iterator(); iterator.hasNext();) {
      var session = iterator.next();
      var expirySeconds = config.rootSection.commands.itemPredicateParser.predicateInteractionExpirySeconds;

      if (session.isExpired(expirySeconds)) {
        iterator.remove();

        config.rootSection.playerMessages.predicateInteractionExpired.sendMessage(
          session.player,
          new InterpretationEnvironment()
            .withVariable("expiry_seconds", expirySeconds)
        );

        if (session.allowMultiUse)
          session.player.sendActionBar(Component.empty()); // Immediately clear action-bar signal

        continue;
      }

      if (session.allowMultiUse)
        config.rootSection.playerMessages.predicateInteractionMultiModeActionBarSignal.sendActionBar(session.player);
    }
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

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    interactionSessionByPlayerId.remove(event.getPlayer().getUniqueId());
  }

  @EventHandler
  public void onInteract(PlayerInteractEvent event) {
    var player = event.getPlayer();
    var action = event.getAction();

    if (player.isSneaking() && (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK)) {
      var interactionSession = interactionSessionByPlayerId.get(player.getUniqueId());

      if (interactionSession != null) {
        event.setCancelled(true);

        if (!interactionSession.allowMultiUse) {
          interactionSession.allowMultiUse = true;
          interactionSession.touchExpiry();
          config.rootSection.playerMessages.predicateInteractionMultiModeEntered.sendMessage(player);
          return;
        }

        interactionSessionByPlayerId.remove(player.getUniqueId());
        player.sendActionBar(Component.empty()); // Immediately clear action-bar signal
        config.rootSection.playerMessages.predicateInteractionMultiModeExited.sendMessage(player);
        return;
      }
    }

    if (!(action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK))
      return;

    if(event.getHand() == EquipmentSlot.OFF_HAND)
      return;

    var clickedBlock = event.getClickedBlock();

    if (clickedBlock == null)
      return;

    if (handleInteractionSessionAndGetIfCancel(player, clickedBlock))
      event.setCancelled(true);
  }

  @EventHandler
  public void onBlockPlace(BlockPlaceEvent event) {
    var player = event.getPlayer();

    if (handleInteractionSessionAndGetIfCancel(player, event.getBlockAgainst()))
      event.setCancelled(true);
  }

  @EventHandler
  public void onBlockBreak(BlockBreakEvent event) {
    var player = event.getPlayer();

    if (handleInteractionSessionAndGetIfCancel(player, event.getBlock()))
      event.setCancelled(true);
  }

  @EventHandler
  public void onBucketEmpty(PlayerBucketEmptyEvent event) {
    var player = event.getPlayer();

    if (handleInteractionSessionAndGetIfCancel(player, event.getBlockClicked()))
      event.setCancelled(true);
  }

  private boolean handleInteractionSessionAndGetIfCancel(Player player, Block target) {
    var interactionSession = interactionSessionByPlayerId.get(player.getUniqueId());

    if (interactionSession == null)
      return false;

    if (!interactionSession.allowMultiUse)
      interactionSessionByPlayerId.remove(player.getUniqueId());

    interactionSession.interactionHandler.accept(target);
    interactionSession.touchExpiry();

    return true;
  }
}
