package me.blvckbytes.storage_query;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StorageQueryCommand implements CommandExecutor, TabCompleter {

  private static final int MAX_COMPLETER_RESULTS = 5;

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

    player.sendMessage("§cHello, world!");
    return true;
  }

  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    if (!(sender instanceof Player player))
      return null;

    List<TranslatedTranslatable> result;

    var completerInput = args[args.length - 1];
    var start = System.nanoTime();

    if (label.equals("lagersuche"))
      result = registryGerman.search(completerInput, MAX_COMPLETER_RESULTS);
    else if (label.equals("storagequery"))
      result = registryEnglish.search(completerInput, MAX_COMPLETER_RESULTS);
    else
      return null;

    var end = System.nanoTime();

    player.sendMessage("§aTook " + (end - start) / 1000.0 / 1000.0 + "ms");

    return result
            .stream()
            .map(TranslatedTranslatable::normalizedName)
            .toList();
  }
}
