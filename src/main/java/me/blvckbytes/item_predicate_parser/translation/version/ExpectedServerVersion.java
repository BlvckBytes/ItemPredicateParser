package me.blvckbytes.item_predicate_parser.translation.version;

public enum ExpectedServerVersion {
  V1_20(1, 20, 0)
  ;

  public final int major;
  public final int minor;
  public final int patch;

  ExpectedServerVersion(int major, int minor, int patch) {
    this.major = major;
    this.minor = minor;
    this.patch = patch;
  }

  public int compare(DetectedServerVersion version) {
    if (major > version.major()) return 1;
    if (major < version.major()) return -1;

    if (minor > version.minor()) return 1;
    if (minor < version.minor()) return -1;

    return Integer.compare(patch, version.patch());
  }
}
