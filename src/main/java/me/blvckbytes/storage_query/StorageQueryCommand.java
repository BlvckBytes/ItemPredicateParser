package me.blvckbytes.storage_query;

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
import java.util.stream.Collectors;

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

    TranslationRegistry registry = decideRegistry(label);

    if (registry == null)
      return false;

    try {
      var tokens = TokenParser.parseTokens(args);
      var predicates = PredicateParser.parsePredicates(tokens, registry);

      if (predicates.isEmpty()) {
        player.sendMessage("§cStorageQuery | Please enter at least one criterion");
        return true;
      }

      var nearChests = findNearChestBlocks(player.getLocation());
      var items = applyPredicates(nearChests, predicates);

      if (items.isEmpty()) {
        player.sendMessage("§cStorageQuery | Couldn't locate any matching items");
        return true;
      }

      player.sendMessage("§aStorageQuery | Located " + items.size() + " matching items");
      this.resultDisplay.displayItems(player, items);
    } catch (ArgumentParseException e) {
      player.sendMessage("§cStorageQuery | " + generateParseExceptionMessage(args, e));
    }

    return true;
  }

  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    if (!(sender instanceof Player player))
      return null;

    TranslationRegistry registry = decideRegistry(label);

    if (registry == null)
      return null;

    try {
      var tokens = TokenParser.parseTokens(args);

      try {
        var currentCommandRepresentation = PredicateParser.parsePredicates(tokens, registry)
          .stream().map(ItemPredicate::stringify)
          .collect(Collectors.joining(" "));

        showParseMessage(player, "§a" + currentCommandRepresentation);
      } catch (ArgumentParseException e) {
        showParseMessage(player, generateParseExceptionMessage(args, e));
        return null;
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

  private List<ChestItem> applyPredicates(List<Chest> chests, List<ItemPredicate> predicates) {
    var items = new ArrayList<ChestItem>();

    for (var nearChest : chests) {
      var inventory = nearChest.getInventory();
      var inventorySize = inventory.getSize();

      for (var slot = 0; slot < inventorySize; ++slot) {
        var currentItem = inventory.getItem(slot);

        if (currentItem == null || currentItem.getType() == Material.AIR)
          continue;

        var didMatch = true;

        for (var predicate : predicates) {
          if (!predicate.test(currentItem)) {
            didMatch = false;
            break;
          }
        }

        if (didMatch)
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

  private @Nullable TranslationRegistry decideRegistry(String commandLabel) {
    return switch (commandLabel.toLowerCase()) {
      case "lagersuche" -> registryGerman;
      case "storagequery" -> registryEnglish;
      default -> null;
    };
  }

  private String generateParseExceptionMessage(String[] args, ArgumentParseException exception) {
    var faultyArgument = args[exception.getArgumentIndex()];
    var conflictMessage = switch (exception.getConflict()) {
      case EXPECTED_INTEGER -> "Expected an integer";
      case EXPECTED_SEARCH_PATTERN -> "Expected a name to search for";
      case NO_SEARCH_MATCH -> "Found no matches";
      case MALFORMED_STRING_ARGUMENT -> "Malformed string notation";
      case MISSING_STRING_TERMINATION -> "String has not been terminated";
      case UNIMPLEMENTED_TRANSLATABLE -> "Unimplemented translatable; please report this";
      case MULTIPLE_SEARCH_PATTERN_WILDCARDS -> "Used multiple ? within one argument";
    };

    return "§c" + conflictMessage + "§7: §4" + faultyArgument;
  }
}
