package me.blvckbytes.item_predicate_parser;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NameScopedKeyValueStore {

  public static final String KEY_LANGUAGE = "language";

  private static final Gson GSON_INSTANCE = new GsonBuilder().setPrettyPrinting().create();

  private final File dataFile;
  private final Logger logger;

  private final Map<String, Map<String, String>> valueByKeyByName;
  private boolean isDataDirty;

  public NameScopedKeyValueStore(File dataFile, Logger logger) {
    this.dataFile = dataFile;
    this.logger = logger;
    this.valueByKeyByName = new HashMap<>();

    loadFromDisk();
  }

  public void write(String name, String key, String value) {
    isDataDirty = true;

    valueByKeyByName
      .computeIfAbsent(name.toLowerCase(), k -> new HashMap<>())
      .put(key.toLowerCase(), value);
  }

  public @Nullable String read(String name, String key) {
    var valueByKey = valueByKeyByName.get(name.toLowerCase());

    if (valueByKey == null)
      return null;

    return valueByKey.getOrDefault(key.toLowerCase(), null);
  }

  private void loadFromDisk() {
    if (!dataFile.isFile())
      return;

    try (
      var reader = new FileReader(dataFile, Charsets.UTF_8);
    ) {
      var jsonData = GSON_INSTANCE.fromJson(reader, JsonObject.class);

      if (jsonData == null)
        return;

      for (var jsonEntry : jsonData.entrySet()) {
        var name = jsonEntry.getKey();

        if (!(jsonEntry.getValue() instanceof JsonObject valueByKeyObject)) {
          logger.warning("Value at \"" + name + "\" of store-file at " + dataFile + " was not a map");
          continue;
        }

        var valueByKey = new HashMap<String, String>();

        for (var stampEntry : valueByKeyObject.entrySet()) {
          var key = stampEntry.getKey();

          if (!(stampEntry.getValue() instanceof JsonPrimitive valuePrimitive)) {
            logger.warning("Value at \"" + name + "\" and key \"" + name + "\" of store-file at " + dataFile + " was not a primitive");
            continue;
          }

          valueByKey.put(key, valuePrimitive.getAsString());
        }

        valueByKeyByName.put(name, valueByKey);
      }
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Could not read state-file at " + dataFile, e);
    }
  }

  public void saveToDisk() {
    if (!isDataDirty)
      return;

    isDataDirty = false;

    try (
      var fileWriter = new FileWriter(dataFile, Charsets.UTF_8);
      var jsonWriter = new JsonWriter(fileWriter);
    ) {
      GSON_INSTANCE.toJson(this.valueByKeyByName, Map.class, jsonWriter);
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Could not write state-file to " + dataFile, e);
    }
  }
}
