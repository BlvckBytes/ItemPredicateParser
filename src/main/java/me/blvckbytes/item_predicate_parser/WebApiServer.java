package me.blvckbytes.item_predicate_parser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import me.blvckbytes.item_predicate_parser.config.MainSection;
import me.blvckbytes.item_predicate_parser.translation.LanguageRegistry;
import me.blvckbytes.item_predicate_parser.translation.TranslationLanguage;
import me.blvckbytes.item_predicate_parser.translation.keyed.VariableKey;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

public class WebApiServer {

  // Do not serialize nulls and do not pretty-print - save as much bandwidth as possible.
  private static final Gson GSON_INSTANCE = new GsonBuilder().create();

  private final LanguageRegistry languageRegistry;
  private final ConfigKeeper<MainSection> config;
  private final Logger logger;
  private @Nullable HttpServer server;

  private final EnumMap<TranslationLanguage, String> prebuiltResponseByLanguage;
  private String prebuiltLanguagesResponse;

  public WebApiServer(
    LanguageRegistry languageRegistry,
    ConfigKeeper<MainSection> config,
    Logger logger
  ) {
    this.languageRegistry = languageRegistry;
    this.config = config;
    this.logger = logger;

    this.prebuiltResponseByLanguage = new EnumMap<>(TranslationLanguage.class);

    config.registerReloadListener(() -> {
      makeAndUpdatePrebuiltResponses();
      restart();
    });

    makeAndUpdatePrebuiltResponses();
  }

  private void makeAndUpdatePrebuiltResponses() {
    prebuiltResponseByLanguage.clear();

    var availableLanguages = new JsonArray();

    for (var language : TranslationLanguage.values()) {
      var availableLanguage = new JsonObject();
      availableLanguages.add(availableLanguage);

      availableLanguage.addProperty("enumName", language.name());
      availableLanguage.addProperty("displayName", TranslationLanguage.matcher.getNormalizedName(language));
    }

    prebuiltLanguagesResponse = GSON_INSTANCE.toJson(availableLanguages);

    for (var language : TranslationLanguage.values()) {
      var variables = new JsonArray();

      var translationRegistry = languageRegistry.getTranslationRegistry(language);
      var availableVariables = translationRegistry.lookup(VariableKey.class);
      availableVariables.sort(Comparator.comparing(it -> it.langKeyed.getWrapped().defaultName));

      for (var availableVariable : availableVariables) {
        var materialDisplayNames = new JsonArray();
        var inheritedMaterialDisplayNames = new JsonArray();

        var variable = availableVariable.langKeyed.getWrapped();

        for (var material : variable.materials) {
          var translation = translationRegistry.getTranslationBySingleton(material);

          if (translation == null)
            translation = material.name();

          materialDisplayNames.add(translation);
        }

        for (var material : variable.getInheritedMaterials()) {
          var translation = translationRegistry.getTranslationBySingleton(material);

          if (translation == null)
            translation = material.name();

          inheritedMaterialDisplayNames.add(translation);
        }

        var parentDisplayNames = new JsonArray();

        for (var parent : variable.parents)
          parentDisplayNames.add(parent.getFinalName(language));

        var variableEntry = new JsonObject();
        variables.add(variableEntry);

        variableEntry.addProperty("displayName", availableVariable.normalizedUnPrefixedTranslation);
        variableEntry.add("materialDisplayNames", materialDisplayNames);
        variableEntry.add("inheritedMaterialDisplayNames", inheritedMaterialDisplayNames);
        variableEntry.add("parentDisplayNames", parentDisplayNames);
      }

      prebuiltResponseByLanguage.put(language, GSON_INSTANCE.toJson(variables));
    }
  }

  public void stop() {
    if (server != null) {
      try {
        server.stop(0);
        server = null;
        logger.info("Stopped the API web-server!");
      } catch (Throwable e) {
        logger.log(Level.SEVERE, "An error occurred while trying to stop the API web-server", e);
      }
    }
  }

  public void restart() {
    stop();

    var port = config.rootSection.variablesServerPort;

    if (port <= 0) {
      logger.info("The API web-server is not enabled.");
      return;
    }

    try {
      server = HttpServer.create(new InetSocketAddress(port), 0);
      server.setExecutor(Executors.newFixedThreadPool(10));

      server.createContext("/languages", exchange -> {
        if (prebuiltLanguagesResponse == null) {
          logger.log(Level.SEVERE, "Missed prebuilt response for existing languages");
          respondPossiblyJsonText(exchange, 500, "Internal error");
          return;
        }

        respondPossiblyJsonText(exchange, 200, prebuiltLanguagesResponse);
      });

      server.createContext("/variables", exchange -> {
        var queryArgs = queryToMap(exchange.getRequestURI().getRawQuery());

        try {
          var languageString = queryArgs.get("language");
          var language = config.rootSection.defaultSelectedLanguage;

          if (languageString != null) {
            try {
              language = TranslationLanguage.valueOf(languageString);
            } catch (Throwable ignored) {}
          }

          var prebuiltResponse = prebuiltResponseByLanguage.get(language);

          if (prebuiltResponse == null) {
            logger.log(Level.SEVERE, "Missed prebuilt response for language " + language);
            respondPossiblyJsonText(exchange, 500, "Internal error");
            return;
          }

          respondPossiblyJsonText(exchange, 200, prebuiltResponse);
        } catch (Throwable e) {
          logger.log(Level.SEVERE, "An error occurred on API web-server while serving /variables", e);
          respondPossiblyJsonText(exchange, 500, "Internal error");
        }
      });

      server.start();
      logger.info("Started the API web-server; listening on :" + port + "!");
    } catch (Throwable e) {
      logger.log(Level.SEVERE, "An error occurred while trying to set up the API web-server", e);
    }
  }

  private void respondPossiblyJsonText(HttpExchange exchange, int statusCode, String payload) {
    try {
      var payloadBytes = payload.getBytes(StandardCharsets.UTF_8);

      var acceptEncoding = exchange.getRequestHeaders().getFirst("Accept-Encoding");
      var gzipSupported = acceptEncoding != null && acceptEncoding.contains("gzip");

      var responseHeaders = exchange.getResponseHeaders();

      if (gzipSupported) {
        responseHeaders.set("Content-Encoding", "gzip");

        var byteStream = new ByteArrayOutputStream();

        try ( var gzipStream = new GZIPOutputStream(byteStream)) {
          gzipStream.write(payloadBytes);
        }

        payloadBytes = byteStream.toByteArray();
      }

      var mimeType = statusCode == 200 ? "application/json" : "text/plain";

      responseHeaders.add("Content-Type", mimeType + "; charset=UTF-8");
      responseHeaders.add("Access-Control-Allow-Origin", "*");
      responseHeaders.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
      responseHeaders.add("Access-Control-Allow-Headers", "*");

      exchange.sendResponseHeaders(statusCode, payloadBytes.length);

      try (OutputStream os = exchange.getResponseBody()) {
        os.write(payloadBytes);
      }
    } catch (Throwable e) {
      logger.log(Level.SEVERE, "An error occurred on the API web-server while trying to respond", e);
    }
  }

  private Map<String, String> queryToMap(String query) {
    var result = new HashMap<String, String>();

    if (query == null || query.isEmpty())
      return result;

    for (var param : query.split("&")) {
      var entry = param.split("=");
      var key = URLDecoder.decode(entry[0], StandardCharsets.UTF_8);
      var value = entry.length > 1 ? URLDecoder.decode(entry[1], StandardCharsets.UTF_8) : "";
      result.put(key, value);
    }

    return result;
  }
}
