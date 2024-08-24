package me.blvckbytes.storage_query;

import com.google.common.primitives.Ints;
import me.blvckbytes.storage_query.argument.Argument;
import me.blvckbytes.storage_query.argument.IntegerArgument;
import me.blvckbytes.storage_query.argument.QuotedStringArgument;
import me.blvckbytes.storage_query.argument.UnquotedStringArgument;
import me.blvckbytes.storage_query.parse.ArgumentParseException;
import me.blvckbytes.storage_query.parse.ParseConflict;
import me.blvckbytes.storage_query.predicate.*;
import me.blvckbytes.storage_query.translation.TranslatedTranslatable;
import me.blvckbytes.storage_query.translation.TranslationRegistry;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
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

    player.sendMessage("§aIt works.");
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
      var parsedArgs = parseArguments(args);

      try {
        var currentCommandRepresentation = parseTokens(parsedArgs, registry)
          .stream().map(ItemPredicate::stringify)
          .collect(Collectors.joining(" "));

        player.sendMessage("§a" + currentCommandRepresentation);
      } catch (ArgumentParseException e) {
        player.sendMessage(generateParseExceptionMessage(args, e));
        return null;
      }

      var parsedArgsCount = parsedArgs.size();

      if (parsedArgsCount == 0)
        return null;

      var lastArg = parsedArgs.get(parsedArgsCount - 1);

      if (!(lastArg instanceof UnquotedStringArgument stringArg))
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

  private List<ItemPredicate> parseTokens(List<Argument> tokens, TranslationRegistry registry) {
    var result = new ArrayList<ItemPredicate>();
    var remainingTokens = new ArrayList<>(tokens);

    while (!remainingTokens.isEmpty()) {
      var currentToken = remainingTokens.removeFirst();

      if (currentToken instanceof QuotedStringArgument textSearch) {
        result.add(new TextSearchPredicate(textSearch.value()));
        continue;
      }

      if (!(currentToken instanceof UnquotedStringArgument translationSearch))
        throw new ArgumentParseException(currentToken.getCommandArgumentIndex(), ParseConflict.EXPECTED_SEARCH_PATTERN);

      var searchString = translationSearch.value();

      if (searchString.isEmpty())
        continue;

      var searchResults = registry.search(searchString);
      var shortestMatch = getShortestMatch(searchResults);

      if (shortestMatch == null)
        throw new ArgumentParseException(((UnquotedStringArgument) currentToken).commandArgumentIndex(), ParseConflict.NO_SEARCH_MATCH);

      if (shortestMatch.translatable() instanceof Material predicateMaterial) {
        result.add(new MaterialPredicate(shortestMatch, predicateMaterial));
        continue;
      }

      if (shortestMatch.translatable() instanceof Enchantment predicateEnchantment) {
        IntegerArgument enchantmentLevel = tryConsumeIntegerArgument(remainingTokens);
        result.add(new EnchantmentPredicate(shortestMatch, predicateEnchantment, enchantmentLevel));
        continue;
      }

      if (shortestMatch.translatable() instanceof PotionEffectType predicatePotionEffect) {
        IntegerArgument potionEffectAmplifier = tryConsumeIntegerArgument(remainingTokens);
        IntegerArgument potionEffectDuration = tryConsumeIntegerArgument(remainingTokens);
        result.add(new PotionEffectPredicate(shortestMatch, predicatePotionEffect, potionEffectAmplifier, potionEffectDuration));
        continue;
      }

      throw new ArgumentParseException(currentToken.getCommandArgumentIndex(), ParseConflict.UNIMPLEMENTED_TRANSLATABLE);
    }

    return result;
  }

  private @Nullable IntegerArgument tryConsumeIntegerArgument(List<Argument> tokens) {
    IntegerArgument integerArgument = null;

    if (!tokens.isEmpty()) {
      var nextToken = tokens.getFirst();

      if (nextToken instanceof IntegerArgument argument) {
        integerArgument = argument;
        tokens.removeFirst();
      }
    }

    return integerArgument;
  }

  private @Nullable TranslatedTranslatable getShortestMatch(List<TranslatedTranslatable> matches) {
    if (matches.isEmpty())
      return null;

    var numberOfMatches = matches.size();

    if (numberOfMatches == 1)
      return matches.getFirst();

    var shortestMatchLength = Integer.MAX_VALUE;
    var shortestMatchIndex = 0;

    for (var matchIndex = 0; matchIndex < numberOfMatches; ++matchIndex) {
      var currentLength = matches.get(matchIndex).translation().length();

      if (currentLength < shortestMatchLength) {
        shortestMatchLength = currentLength;
        shortestMatchIndex = matchIndex;
      }
    }

    return matches.get(shortestMatchIndex);
  }

  private List<Argument> parseArguments(String[] args) {
    var result = new ArrayList<Argument>();

    var stringBeginArgumentIndex = 0;
    var stringContents = new StringBuilder();

    for (var argumentIndex = 0; argumentIndex < args.length; ++argumentIndex) {
      var arg = args[argumentIndex];
      var argLength = arg.length();

      if (argLength == 0) {
        result.add(new UnquotedStringArgument(argumentIndex, ""));
        continue;
      }

      var firstChar = arg.charAt(0);

      if (firstChar == '"') {
        var terminationIndex = arg.indexOf('"', 1);

        // Argument contains both the start- and end-marker
        if (terminationIndex > 0) {
          if (arg.indexOf('"', terminationIndex + 1) != -1)
            throw new ArgumentParseException(argumentIndex, ParseConflict.MALFORMED_STRING_ARGUMENT);

          result.add(new QuotedStringArgument(argumentIndex, arg.substring(1, argLength - 1)));
          continue;
        }

        // Contains only one double-quote, which is leading

        // Terminated a string which contains a trailing whitespace (valid use-case)
        if (!stringContents.isEmpty()) {
          if (argLength != 1)
            throw new ArgumentParseException(argumentIndex, ParseConflict.MALFORMED_STRING_ARGUMENT);

          stringContents.append(' ');
          result.add(new QuotedStringArgument(stringBeginArgumentIndex, stringContents.toString()));
          stringContents.setLength(0);
          continue;
        }

        // Started a string which contains a leading whitespace (valid use-case)
        if (argLength == 1) {
          stringBeginArgumentIndex = argumentIndex;
          stringContents.append(' ');
          continue;
        }

        // Multi-arg string beginning
        stringBeginArgumentIndex = argumentIndex;
        stringContents.append(arg, 1, argLength);
        continue;
      }

      if (arg.charAt(argLength - 1) == '"') {
        if (stringContents.isEmpty())
          throw new ArgumentParseException(argumentIndex, ParseConflict.MALFORMED_STRING_ARGUMENT);

        // Multi-arg string termination
        stringContents.append(' ').append(arg, 0, argLength - 1);
        result.add(new QuotedStringArgument(stringBeginArgumentIndex, stringContents.toString()));
        stringContents.setLength(0);
        continue;
      }

      // Within string
      if (!stringContents.isEmpty()) {
        stringContents.append(' ').append(arg);
        continue;
      }

      if (firstChar == '*' && argLength == 1) {
        result.add(new IntegerArgument(argumentIndex, null));
        continue;
      }

      // No names will have a leading digit; expect integer
      if (Character.isDigit(firstChar)) {
        var numericArgument = Ints.tryParse(arg);

        if (numericArgument == null)
          throw new ArgumentParseException(argumentIndex, ParseConflict.EXPECTED_INTEGER);

        result.add(new IntegerArgument(argumentIndex, numericArgument));
        continue;
      }

      // Ensure that there are no quotes wedged into search-terms
      for (var argIndex = 0; argIndex < argLength; ++argIndex) {
        if (arg.charAt(argIndex) == '"')
          throw new ArgumentParseException(argumentIndex, ParseConflict.MALFORMED_STRING_ARGUMENT);
      }

      result.add(new UnquotedStringArgument(argumentIndex, arg));
    }

    if (!stringContents.isEmpty())
      throw new ArgumentParseException(args.length - 1, ParseConflict.MISSING_STRING_TERMINATION);

    return result;
  }
}
