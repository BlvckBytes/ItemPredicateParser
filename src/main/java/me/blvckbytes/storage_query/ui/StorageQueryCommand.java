package me.blvckbytes.storage_query.ui;

import me.blvckbytes.storage_query.parse.ParseConflict;
import me.blvckbytes.storage_query.token.UnquotedStringToken;
import me.blvckbytes.storage_query.parse.ArgumentParseException;
import me.blvckbytes.storage_query.parse.PredicateParser;
import me.blvckbytes.storage_query.parse.TokenParser;
import me.blvckbytes.storage_query.predicate.*;
import me.blvckbytes.storage_query.parse.SearchWildcardPresence;
import me.blvckbytes.storage_query.translation.TranslatedTranslatable;
import me.blvckbytes.storage_query.translation.TranslationRegistry;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class StorageQueryCommand implements CommandExecutor, TabCompleter {

  private static final int MAX_COMPLETER_RESULTS = 15;
  private static final int CHEST_SEARCH_DISTANCE_HALF = 50;

  private final TranslationRegistry registryGerman;
  private final TranslationRegistry registryEnglish;
  private final ResultDisplayHandler resultDisplay;

  public StorageQueryCommand(
    TranslationRegistry registryGerman,
    TranslationRegistry registryEnglish,
    ResultDisplayHandler resultDisplay
  ) {
    this.registryGerman = registryGerman;
    this.registryEnglish = registryEnglish;
    this.resultDisplay = resultDisplay;
  }

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
    if (!(sender instanceof Player player))
      return false;

    return switch (label.toLowerCase()) {
      case "lagersuche" -> {
        handleCommand(player, args, registryGerman);
        yield true;
      }
      case "storagequery" -> {
        handleCommand(player, args, registryEnglish);
        yield true;
      }
      default -> false;
    };
  }

  private void handleCommand(Player player, String[] args, TranslationRegistry registry) {
    try {
      var parsingStart = System.nanoTime();
      var tokens = TokenParser.parseTokens(args);
      var ast = new PredicateParser(registry, tokens, false).parseAst();

      if (ast == null) {
        player.sendMessage("§cStorageQuery | Please enter at least one criterion");
        return;
      }

      var parsingEnd = System.nanoTime();
      player.sendMessage(String.format("§6(Parsing took %.2f ms)", (parsingEnd - parsingStart) / 1000.0 / 1000.0));

      var chestSearchStart = System.nanoTime();
      var nearChests = findNearChestBlocks(player.getLocation());
      var chestSearchEnd = System.nanoTime();
      player.sendMessage(String.format("§6(Chest-Search took %.2f ms)", (chestSearchEnd - chestSearchStart) / 1000.0 / 1000.0));

      var predicateStart = System.nanoTime();
      var items = applyAst(nearChests, ast);
      var predicateEnd = System.nanoTime();
      player.sendMessage(String.format("§6(Predicates took %.2f ms)", (predicateEnd - predicateStart) / 1000.0 / 1000.0));

      if (items.isEmpty()) {
        player.sendMessage("§cStorageQuery | Couldn't locate any matching items");
        return;
      }

      player.sendMessage("§aStorageQuery | Located " + items.size() + " matching items");
      this.resultDisplay.displayItems(player, items);
    } catch (ArgumentParseException e) {
      player.sendMessage("§cStorageQuery | " + generateParseExceptionMessage(args, e));
    }
  }

  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    if (!(sender instanceof Player player))
      return null;

    return switch (label.toLowerCase()) {
      case "lagersuche" -> handleTabComplete(player, args, registryGerman);
      case "storagequery" -> handleTabComplete(player, args, registryEnglish);
      default -> null;
    };
  }

  private @Nullable List<String> handleTabComplete(Player player, String[] args, TranslationRegistry registry) {
    try {
      var tokens = TokenParser.parseTokens(args);

      try {
        // Allow for missing closing parentheses, as hotbar-previewing would become useless otherwise
        var currentCommandRepresentation = new PredicateParser(registry, tokens, true).parseAst();

        if (currentCommandRepresentation != null)
          showParseMessage(player, "§a" + currentCommandRepresentation.stringify(false));
      } catch (ArgumentParseException e) {
        showParseMessage(player, generateParseExceptionMessage(args, e));
      }

      var parsedArgsCount = tokens.size();

      if (parsedArgsCount == 0)
        return null;

      var argumentIndex = parsedArgsCount - 1;
      var lastArg = tokens.get(argumentIndex);

      if (!(lastArg instanceof UnquotedStringToken stringArg))
        return null;

      var searchText = stringArg.value();

      if (searchText.isEmpty())
        return List.of();

      var searchResult = registry.search(searchText);

      if (searchResult.wildcardPresence() == SearchWildcardPresence.CONFLICT_OCCURRED_REPEATEDLY)
        throw new ArgumentParseException(argumentIndex, ParseConflict.MULTIPLE_SEARCH_PATTERN_WILDCARDS);

      return searchResult.result()
        .stream()
        .map(TranslatedTranslatable::normalizedName)
        .sorted(Comparator.comparing(String::length))
        .limit(MAX_COMPLETER_RESULTS)
        .toList();
    } catch (ArgumentParseException e) {
      showParseMessage(player, generateParseExceptionMessage(args, e));
      return null;
    }
  }

  private List<ChestItem> applyAst(List<Chest> chests, ItemPredicate ast) {
    var items = new ArrayList<ChestItem>();

    for (var nearChest : chests) {
      var inventory = nearChest.getInventory();
      var inventorySize = inventory.getSize();

      for (var slot = 0; slot < inventorySize; ++slot) {
        var currentItem = inventory.getItem(slot);

        if (currentItem == null || currentItem.getType() == Material.AIR)
          continue;

        if (ast.test(currentItem))
          items.add(new ChestItem(nearChest, slot));
      }
    }

    return items;
  }

  private List<Chest> findNearChestBlocks(Location location) {
    var world = Objects.requireNonNull(location.getWorld());
    var result = new ArrayList<Chest>();
    var ignoredLocations = new HashSet<Location>();

    for (int dx = -CHEST_SEARCH_DISTANCE_HALF; dx <= CHEST_SEARCH_DISTANCE_HALF; ++dx) {
      for (int dy = -CHEST_SEARCH_DISTANCE_HALF; dy <= CHEST_SEARCH_DISTANCE_HALF; ++dy) {
        for (int dz = -CHEST_SEARCH_DISTANCE_HALF; dz <= CHEST_SEARCH_DISTANCE_HALF; ++dz) {
          var currentLocation = new Location(
            world,
            dx + location.getBlockX(),
            dy + location.getBlockY(),
            dz + location.getBlockZ()
          );

          if (ignoredLocations.contains(currentLocation))
            continue;

          var currentBlock = world.getBlockAt(currentLocation);

          if (!(currentBlock.getState() instanceof Chest chestBlock))
            continue;

          if (chestBlock.getInventory() instanceof DoubleChestInventory doubleChestInventory) {
            var leftLocation = Objects.requireNonNull(doubleChestInventory.getLeftSide().getLocation());

            if (!leftLocation.equals(currentLocation))
              ignoredLocations.add(leftLocation);
            else
              ignoredLocations.add(Objects.requireNonNull(doubleChestInventory.getRightSide().getLocation()));
          }

          result.add(chestBlock);
        }
      }
    }

    return result;
  }

  private void showParseMessage(Player player, String message) {
    // The action-bar likely makes the best spot for rapidly updating messages
    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
  }

  private String generateParseExceptionMessage(String[] args, ArgumentParseException exception) {
    var markedExpression = new StringJoiner(" ");

    for (var argIndex = 0; argIndex < args.length; ++argIndex) {
      if (exception.getArgumentIndex() == argIndex)
        markedExpression.add("§4" + args[argIndex]);
      else
        markedExpression.add("§c" + args[argIndex]);
    }

    var conflictMessage = switch (exception.getConflict()) {
      case EXPECTED_INTEGER -> "Expected an integer";
      case EXPECTED_SEARCH_PATTERN -> "Expected a name to search for";
      case NO_SEARCH_MATCH -> "Found no matches";
      case MALFORMED_STRING_ARGUMENT -> "Malformed string notation";
      case MISSING_STRING_TERMINATION -> "String has not been terminated";
      case UNIMPLEMENTED_TRANSLATABLE -> "Unimplemented translatable; please report this";
      case MULTIPLE_SEARCH_PATTERN_WILDCARDS -> "Used multiple ? within one argument";
      case DOES_NOT_ACCEPT_TIME_NOTATION -> "This argument does not accept time notation";
      case EXPECTED_EXPRESSION_AFTER_JUNCTION -> "This junction has to to be followed up by another expression";
      case EXPECTED_OPENING_PARENTHESIS -> "Expected a opening parenthesis";
      case EXPECTED_CLOSING_PARENTHESIS -> "Expected a closing parenthesis";
    };

    return "§c" + conflictMessage + "§7: " + markedExpression;
  }
}
