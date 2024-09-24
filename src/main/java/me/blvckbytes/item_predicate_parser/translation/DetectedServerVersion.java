package me.blvckbytes.item_predicate_parser.translation;

public record DetectedServerVersion(
  int major,
  int minor,
  int patch,
  String original
) {

  public static DetectedServerVersion fromString(String value) {
    var versionParts = value.split("\\.");

    if (versionParts.length < 2 || versionParts.length > 3)
      throw new IllegalStateException("Unexpected number of version-parts: " + versionParts.length + " in " + value);

    try {
      return new DetectedServerVersion(
        Integer.parseInt(versionParts[0]),
        Integer.parseInt(versionParts[1]),
        versionParts.length == 3 ? Integer.parseInt(versionParts[2]) : 0,
        value
      );
    } catch (NumberFormatException e) {
      throw new IllegalStateException("Could not parse a version-part within " + value, e);
    }
  }
}
