package me.blvckbytes.item_predicate_parser.translation.version;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.logging.Logger;

public class VersionDependentCodeFactory {

  private static final MethodHandles.Lookup publicLookup = MethodHandles.publicLookup();
  private static final MethodType constructorType = MethodType.methodType(void.class, DetectedServerVersion.class, Logger.class);

  // TODO: Is this path-approach really necessary? I think instantiating classes with the new-keyword will work just
  //       as well, since symbols shouldn't be resolved if the class is never actually used

  // NOTE: Using path-notation to support relocation, if ever needed
  private static final String GT_1_20_PATH = "me/blvckbytes/item_predicate_parser/translation/version/VersionDependentCode_GT_1_20";
  private static final String E_1_20_PATH = "me/blvckbytes/item_predicate_parser/translation/version/VersionDependentCode_E_1_20";
  private static final String LT_1_20_PATH = "me/blvckbytes/item_predicate_parser/translation/version/VersionDependentCode_LT_1_20";

  private final IVersionDependentCode instance;

  public VersionDependentCodeFactory(DetectedServerVersion serverVersion, Logger logger) throws Throwable {
    Class<?> targetClass;

    if (serverVersion.major() != 1)
      throw new IllegalStateException("Cannot handle a major-version greater than one");

    if (serverVersion.minor() == 20)
      targetClass = Class.forName(E_1_20_PATH.replace('/', '.'));

    else if (serverVersion.minor() > 20)
      targetClass = Class.forName(GT_1_20_PATH.replace('/', '.'));

    else
      targetClass = Class.forName(LT_1_20_PATH.replace('/', '.'));

    var constructorHandle = publicLookup.findConstructor(targetClass, constructorType);
    this.instance = (IVersionDependentCode) constructorHandle.invoke(serverVersion, logger);
  }

  public IVersionDependentCode get() {
    return instance;
  }
}
