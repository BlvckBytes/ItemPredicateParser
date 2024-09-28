package me.blvckbytes.item_predicate_parser.translation;

import me.blvckbytes.item_predicate_parser.translation.version.DetectedServerVersion;
import me.blvckbytes.item_predicate_parser.translation.version.ExpectedServerVersion;
import me.blvckbytes.item_predicate_parser.translation.version.IVersionDependentCode;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.logging.Logger;

public class VersionDependentCodeFactory {

  private static final MethodHandles.Lookup publicLookup = MethodHandles.publicLookup();
  private static final MethodType constructorType = MethodType.methodType(void.class, DetectedServerVersion.class, Logger.class);

  // NOTE: Using path-notation to support relocation, if ever needed
  private static final String GTE_1_20_PATH = "me/blvckbytes/item_predicate_parser/translation/version/VersionDependentCode_GTE_1_20";
  private static final String LT_1_20_PATH = "me/blvckbytes/item_predicate_parser/translation/version/VersionDependentCode_LT_1_20";

  private final IVersionDependentCode instance;

  public VersionDependentCodeFactory(DetectedServerVersion serverVersion, Logger logger) throws Throwable {
    Class<?> targetClass;

    // 1.20 is greater than current => LT 1.20
    if (ExpectedServerVersion.V1_20.compare(serverVersion) > 0) {
      targetClass = Class.forName(LT_1_20_PATH.replace('/', '.'));
      logger.info("Detected version < 1.20");
    }

    // 1.20 is less than or equal to current => GTE 1.20
    else {
      targetClass = Class.forName(GTE_1_20_PATH.replace('/', '.'));
      logger.info("Detected version >= 1.20");
    }

    var constructorHandle = publicLookup.findConstructor(targetClass, constructorType);
    this.instance = (IVersionDependentCode) constructorHandle.invoke(serverVersion, logger);
  }

  public IVersionDependentCode get() {
    return instance;
  }
}
