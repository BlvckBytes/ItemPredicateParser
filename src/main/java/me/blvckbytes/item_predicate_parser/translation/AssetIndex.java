package me.blvckbytes.item_predicate_parser.translation;

import com.google.gson.*;
import me.blvckbytes.item_predicate_parser.translation.version.DetectedServerVersion;
import org.bukkit.Bukkit;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class AssetIndex {

  private record ClientEmbeddedResult(
    String fileContents,
    String fileExtension
  ) {}

  private static final String MANIFEST_URL = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";
  private static final String RESOURCE_BASE_URL = "https://resources.download.minecraft.net/";
  private static final String LANG_OBJECT_PREFIX = "minecraft/lang/";

  private static final HttpClient httpClient = HttpClient.newHttpClient();
  private static final Gson gson = new GsonBuilder().create();

  public final DetectedServerVersion serverVersion;
  private final VersionUrls versionUrls;
  private final Map<String, String> languageFileUrls;

  public AssetIndex(String customVersion) throws Exception {
    this.serverVersion = DetectedServerVersion.fromString(customVersion == null ? parseServerVersion() : customVersion);
    this.versionUrls = getUrlsForCurrentVersion();
    this.languageFileUrls = getLanguageFileUrlsForCurrentVersion();
  }

  public JsonObject getLanguageFile(TranslationLanguage language) throws Exception {
    if (language == TranslationLanguage.ENGLISH_US) {
      var embeddedResult = getClientEmbeddedLanguageFileContents();

      if (embeddedResult.fileExtension.equals("json"))
        return parseJson(embeddedResult.fileContents);

      return LangToJsonUtil.convertLangContentsToJsonObject(embeddedResult.fileContents);
    }

    String languageFileUrl;

    if ((languageFileUrl = languageFileUrls.get(language.assetFileNameWithoutExtension + ".json")) != null)
      return parseJson(makePlainTextGetRequest(languageFileUrl));

    if ((languageFileUrl = languageFileUrls.get(language.assetFileNameWithoutExtension + ".lang")) != null)
      return LangToJsonUtil.convertLangContentsToJsonObject(makePlainTextGetRequest(languageFileUrl));

    throw new IllegalStateException("Could not locate language-file url for " + language.assetFileNameWithoutExtension);
  }

  private ClientEmbeddedResult getClientEmbeddedLanguageFileContents() throws Exception {
    try (
      var inputStream = makeGetRequest(versionUrls.clientJarUrl());
      var jarStream = new JarInputStream(new ByteArrayInputStream(inputStream.readAllBytes()))
    ) {
      JarEntry entry;

      while ((entry = jarStream.getNextJarEntry()) != null) {
        var entryName = entry.getName();

        if (!entryName.startsWith("assets/minecraft/lang/en_us"))
          continue;

        byte[] fileData;

        if (entry.getSize() < 0) {
          // The stream automatically "ends" at the current entry's boundary
          fileData = jarStream.readAllBytes();
        } else {
          fileData = new byte[(int) entry.getSize()];
          if (jarStream.read(fileData, 0, fileData.length) != fileData.length)
            throw new IllegalStateException("Could not read the whole language file");
        }

        var fileExtension = entryName.substring(entryName.lastIndexOf('.') + 1);

        return new ClientEmbeddedResult(new String(fileData), fileExtension);
      }

      throw new IllegalStateException("Could not find en_us.json within client.jar");
    }
  }

  private InputStream makeGetRequest(String url) throws Exception {
    var response = httpClient.send(
      HttpRequest
        .newBuilder(URI.create(url))
        .timeout(Duration.ofSeconds(3))
        .GET()
        .build(),
      HttpResponse.BodyHandlers.ofInputStream()
    );

    if (response.statusCode() != 200)
      throw new IllegalStateException("GET \"" + url + "\" responded with non-200 status-code");

    return response.body();
  }

  private String makePlainTextGetRequest(String url) throws Exception {
    try (
      var inputStream = makeGetRequest(url)
    ) {
      return new String(inputStream.readAllBytes());
    }
  }

  private JsonObject parseJson(String string) throws JsonSyntaxException {
    return gson.fromJson(string, JsonObject.class);
  }

  private JsonObject makeJsonGetRequest(String url) throws Exception {
    return gson.fromJson(new InputStreamReader(makeGetRequest(url)), JsonObject.class);
  }

  private JsonArray getVersions() throws Exception {
    var versionsNode = makeJsonGetRequest(MANIFEST_URL).get("versions");

    if (!(versionsNode instanceof JsonArray versionsArray))
      throw new IllegalStateException("Expected top-level key \"versions\" to be a JSON-array");

    return versionsArray;
  }

  private Map<String, String> getLanguageFileUrlsForCurrentVersion() throws Exception {
    var objectsNode = makeJsonGetRequest(versionUrls.assetIndexUrl()).get("objects");

    if (!(objectsNode instanceof JsonObject objectsObject))
      throw new IllegalStateException("Expected top-level key \"objects\" to be a JSON-object");

    var result = new HashMap<String, String>();

    for (var entry : objectsObject.entrySet()) {
      var entryKey = entry.getKey();

      if (!(entryKey.startsWith(LANG_OBJECT_PREFIX)))
        continue;

      var fileName = entryKey.substring(LANG_OBJECT_PREFIX.length());

      if (!(entry.getValue() instanceof JsonObject valueObject))
        throw new IllegalStateException("Expected key \"objects." + entryKey + "\" to be a JSON-object");

      if (!(valueObject.get("hash") instanceof JsonPrimitive valuePrimitive))
        throw new IllegalStateException("Expected key \"objects." + entryKey + ".hash\" to be a JSON-primitive");

      var hashString = valuePrimitive.getAsString();

      var fileUrl = RESOURCE_BASE_URL + hashString.substring(0, 2) + "/" + hashString;
      result.put(fileName, fileUrl);
    }

    return result;
  }

  private VersionUrls getUrlsForCurrentVersion() throws Exception {
    var responseJson = makeJsonGetRequest(getPackageDataUrlForCurrentVersion());

    var assetIndexNode = responseJson.get("assetIndex");

    if (!(assetIndexNode instanceof JsonObject assetsIndexObject))
      throw new IllegalStateException("Expected top-level key \"assetIndex\" to be a JSON-object");

    var assetsIndexUrlNode = assetsIndexObject.get("url");

    if (!(assetsIndexUrlNode instanceof JsonPrimitive assetsIndexUrlPrimitive))
      throw new IllegalStateException("Expected key \"assetIndex.url\" to be a JSON-primitive");

    var assetIndexUrl = assetsIndexUrlPrimitive.getAsString();

    var downloadsNode = responseJson.get("downloads");

    if (!(downloadsNode instanceof JsonObject downloadsObject))
      throw new IllegalStateException("Expected top-level key \"downloads\" to be a JSON-object");

    var clientNode = downloadsObject.get("client");

    if (!(clientNode instanceof JsonObject clientObject))
      throw new IllegalStateException("Expected key \"downloads.client\" to be a JSON-object");

    var clientUrlNode = clientObject.get("url");

    if (!(clientUrlNode instanceof JsonPrimitive clientUrlPrimitive))
      throw new IllegalStateException("Expected key \"downloads.client.url\" to be a JSON-primitive");

    var clientUrl = clientUrlPrimitive.getAsString();

    return new VersionUrls(assetIndexUrl, clientUrl);
  }

  private String getPackageDataUrlForCurrentVersion() throws Exception {
    var versions = getVersions();

    for (var versionNode : versions) {
      if (!(versionNode instanceof JsonObject versionObject))
        throw new IllegalStateException("Expected items of top-level key \"versions\" to be JSON-objects");

      var idNode = versionObject.get("id");

      if (!(idNode instanceof JsonPrimitive idPrimitive))
        throw new IllegalStateException("Expected \"id\" of version-item to be a JSON-primitive");

      var idString = idPrimitive.getAsString();

      if(!idString.equals(serverVersion.original()))
        continue;

      var urlNode = versionObject.get("url");

      if (!(urlNode instanceof JsonPrimitive urlPrimitive))
        throw new IllegalStateException("Expected \"url\" of version-item to be a JSON-primitive");

      return urlPrimitive.getAsString();
    }

    throw new IllegalStateException("Couldn't find a manifest entry for version \"" + serverVersion + "\"");
  }

  private String parseServerVersion() {
    // <fork_name> (MC: <server_version>)
    var fullString =  Bukkit.getServer().getVersion();
    var versionBeginMarker = "(MC: ";

    var markerIndex = fullString.indexOf(versionBeginMarker);

    if (markerIndex < 0)
      throw new IllegalStateException("Could not locate \"" + versionBeginMarker + "\" within \"" + fullString + "\"");

    var firstTargetChar = markerIndex + versionBeginMarker.length();
    var lastTargetChar = fullString.indexOf(')', firstTargetChar + 1);

    if (lastTargetChar < 0)
      throw new IllegalStateException("Could not locate the version end parenthesis within \"" + fullString + "\"");

    return fullString.substring(firstTargetChar, lastTargetChar);
  }
}
