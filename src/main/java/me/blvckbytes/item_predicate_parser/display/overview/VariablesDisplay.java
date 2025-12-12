package me.blvckbytes.item_predicate_parser.display.overview;

import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import me.blvckbytes.item_predicate_parser.config.MainSection;
import me.blvckbytes.item_predicate_parser.display.AsyncTaskQueue;
import me.blvckbytes.item_predicate_parser.display.Display;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class VariablesDisplay extends Display<VariablesDisplayData> {

  private final AsyncTaskQueue asyncQueue;

  private int numberOfPages;

  private IEvaluationEnvironment pageEnvironment;

  private int currentPage = 1;

  public VariablesDisplay(
    ConfigKeeper<MainSection> config,
    Plugin plugin,
    Player player,
    VariablesDisplayData displayData
  ) {
    super(player, displayData, config, plugin);

    this.asyncQueue = new AsyncTaskQueue(plugin);

    setupEnvironments();

    // Within async context already, see corresponding command
    show();
  }

  private void setupEnvironments() {
    var numberOfDisplaySlots = config.rootSection.variablesDisplay.getPaginationSlots().size();
    this.numberOfPages = Math.max(1, (int) Math.ceil(displayData.variables().size() / (double) numberOfDisplaySlots));

    this.pageEnvironment = new EvaluationEnvironmentBuilder()
      .withLiveVariable("current_page", () -> this.currentPage)
      .withLiveVariable("number_pages", () -> this.numberOfPages)
      .build(config.rootSection.variablesDisplay.inventoryEnvironment);
  }

  @Override
  public void onConfigReload() {
    setupEnvironments();
    show();
  }

  public void nextPage() {
    asyncQueue.enqueue(() -> {
      if (currentPage == numberOfPages)
        return;

      ++currentPage;
      show();
    });
  }

  public void previousPage() {
    asyncQueue.enqueue(() -> {
      if (currentPage == 1)
        return;

      --currentPage;
      show();
    });
  }

  public void firstPage() {
    asyncQueue.enqueue(() -> {
      if (currentPage == 1)
        return;

      currentPage = 1;
      show();
    });
  }

  public void lastPage() {
    asyncQueue.enqueue(() -> {
      if (currentPage == numberOfPages)
        return;

      currentPage = numberOfPages;
      show();
    });
  }

  @Override
  public void show() {
    super.show();
  }

  @Override
  protected void renderItems() {
    var displaySlots = config.rootSection.variablesDisplay.getPaginationSlots();
    var itemsIndex = (currentPage - 1) * displaySlots.size();
    var numberOfItems = displayData.variables().size();

    for (var slot : displaySlots) {
      var currentSlot = itemsIndex++;

      if (currentSlot >= numberOfItems) {
        inventory.setItem(slot, null);
        continue;
      }

      var variable = displayData.variables().get(currentSlot);
      var materialTokenLines = makeTokenLines(variable.materialDisplayNames());

      var item = config.rootSection.variablesDisplay.items.variable.build(
        new EvaluationEnvironmentBuilder()
          .withStaticVariable("icon_type", variable.icon().name())
          .withStaticVariable("display_name", variable.displayName())
          .withStaticVariable("material_token_lines", materialTokenLines)
          .build()
      );

      inventory.setItem(slot, item);
    }

    // Render filler first, such that it may be overridden by conditionally displayed items
    config.rootSection.variablesDisplay.items.filler.renderInto(inventory, pageEnvironment);

    config.rootSection.variablesDisplay.items.previousPage.renderInto(inventory, pageEnvironment);
    config.rootSection.variablesDisplay.items.nextPage.renderInto(inventory, pageEnvironment);
  }

  private ArrayList<TokenLine> makeTokenLines(List<String> input) {
    var tokenLines = new ArrayList<TokenLine>();

    var currentLineLength = 0;
    var maxLineLength = config.rootSection.variablesDisplay.maxTokenLineWidth;
    var currentLine = new TokenLine();

    for (var materialDisplayName : input) {
      var nameTokens = materialDisplayName.split(" ");
      var didAddToLine = false;

      for (var nameToken : nameTokens) {
        var tokenLength = nameToken.length();

        if (currentLineLength != 0 && currentLineLength + tokenLength > maxLineLength) {
          if (didAddToLine)
            currentLine.wraps = true;

          tokenLines.add(currentLine);
          currentLine = new TokenLine();
          currentLineLength = 0;
          didAddToLine = false;
        }

        // Spaces in-between
        else if (currentLineLength != 0)
          ++currentLineLength;

        if (!didAddToLine)
          currentLine.add(nameToken);
        else
          currentLine.append(nameToken);

        currentLineLength += tokenLength;
        didAddToLine = true;
      }
    }

    if (!currentLine.isEmpty()) {
      currentLine.wraps = true;
      tokenLines.add(currentLine);
    }

    return tokenLines;
  }

  @Override
  protected Inventory makeInventory() {
    return config.rootSection.variablesDisplay.createInventory(pageEnvironment);
  }
}
