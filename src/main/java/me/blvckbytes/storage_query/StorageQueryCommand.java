package me.blvckbytes.storage_query;

import me.blvckbytes.storage_query.token.UnquotedStringToken;
import me.blvckbytes.storage_query.parse.ArgumentParseException;
import me.blvckbytes.storage_query.parse.PredicateParser;
import me.blvckbytes.storage_query.parse.TokenParser;
import me.blvckbytes.storage_query.predicate.*;
import me.blvckbytes.storage_query.translation.TranslatedTranslatable;
import me.blvckbytes.storage_query.translation.TranslationRegistry;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class StorageQueryCommand implements CommandExecutor, TabCompleter {

  private static final int MAX_COMPLETER_RESULTS = 10;

  private final TranslationRegistry registryGerman;
  private final TranslationRegistry registryEnglish;

  public StorageQueryCommand(
    TranslationRegistry registryGerman,
    TranslationRegistry registryEnglish
  ) {
    this.registryGerman = registryGerman;
    this.registryEnglish = registryEnglish;
  }

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
    if (!(sender instanceof Player player))
      return false;

    TranslationRegistry registry;

    if (label.equals("lagersuche"))
      registry = registryGerman;
    else if (label.equals("storagequery"))
      registry = registryEnglish;
    else
      return false;

    var mainHandItem = player.getInventory().getItemInMainHand();

    if (mainHandItem.getType() == Material.AIR) {
      player.sendMessage("§cNot holding anything in your hand!");
      return true;
    }

    try {
      var tokens = TokenParser.parseTokens(args);

      try {
        var predicates = PredicateParser.parsePredicates(tokens, registry);

        for (var predicate : predicates) {
          if (!predicate.test(mainHandItem)) {
            player.sendMessage("§cItem does not match!");
            return true;
          }
        }

        player.sendMessage("§aItem does match!");
      } catch (ArgumentParseException e) {
        player.sendMessage(generateParseExceptionMessage(args, e));
      }
    } catch (ArgumentParseException e) {
      player.sendMessage(generateParseExceptionMessage(args, e));
    }

    return true;
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
    };

    return "§c" + conflictMessage + "§7: §4" + faultyArgument;
  }

  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    if (!(sender instanceof Player player))
      return null;

    TranslationRegistry registry;

    if (label.equals("lagersuche"))
      registry = registryGerman;
    else if (label.equals("storagequery"))
      registry = registryEnglish;
    else
      return null;

    try {
      var tokens = TokenParser.parseTokens(args);

      try {
        var currentCommandRepresentation = PredicateParser.parsePredicates(tokens, registry)
          .stream().map(ItemPredicate::stringify)
          .collect(Collectors.joining(" "));

        player.sendMessage("§a" + currentCommandRepresentation);
      } catch (ArgumentParseException e) {
        player.sendMessage(generateParseExceptionMessage(args, e));
        return null;
      }

      var parsedArgsCount = tokens.size();

      if (parsedArgsCount == 0)
        return null;

      var lastArg = tokens.get(parsedArgsCount - 1);

      if (!(lastArg instanceof UnquotedStringToken stringArg))
        return null;

      var searchText = stringArg.value();

      if (searchText.isEmpty())
        return List.of();

      return registry.search(searchText)
        .stream()
        .map(TranslatedTranslatable::normalizedName)
        .sorted(Comparator.comparing(String::length))
        .limit(MAX_COMPLETER_RESULTS)
        .toList();
    } catch (ArgumentParseException e) {
      player.sendMessage(generateParseExceptionMessage(args, e));
      return null;
    }
  }
}
