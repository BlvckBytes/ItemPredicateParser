package me.blvckbytes.item_predicate_parser;

import org.apache.commons.lang3.ClassUtils;
import org.hamcrest.Matchers;
import org.jetbrains.annotations.Nullable;
import org.opentest4j.AssertionFailedError;

import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.Modifier;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class RecursiveInterceptedEqualityChecker {

  private final Map<Class<?>, List<Field>> nonStaticFieldsCache;
  private final Map<Class<?>, EqualityPredicate<Object>> interceptors;
  private final Set<String> expectedTypePrefixes;
  private final Set<Class<?>> expectedTypes;

  public RecursiveInterceptedEqualityChecker() {
    this.nonStaticFieldsCache = new HashMap<>();
    this.interceptors = new HashMap<>();
    this.expectedTypePrefixes = new HashSet<>();
    this.expectedTypes = new HashSet<>();
  }

  @SuppressWarnings("unchecked")
  public <T> RecursiveInterceptedEqualityChecker intercept(Class<T> type, EqualityPredicate<T> externalChecker) {
    this.interceptors.put(type, (EqualityPredicate<Object>) externalChecker);
    return this;
  }

  public RecursiveInterceptedEqualityChecker interceptAndUseAssertEquals(Class<?> type) {
    this.interceptors.put(type, ((rootActualType, pathParts, expected, actual) -> {
      assertEquals(expected, actual);
      return true;
    }));

    return this;
  }

  public RecursiveInterceptedEqualityChecker interceptAndReturnTrue(Class<?> type) {
    this.interceptors.put(type, ((rootActualType, pathParts, expected, actual) -> true));
    return this;
  }

  public RecursiveInterceptedEqualityChecker expectTypePrefix(String prefix) {
    this.expectedTypePrefixes.add(prefix);
    return this;
  }

  public RecursiveInterceptedEqualityChecker expectType(Class<?> type) {
    this.expectedTypes.add(type);
    return this;
  }

  public void check(@Nullable Object expected, @Nullable Object actual) {
    compareRecursively(
      actual == null ? null : actual.getClass(),
      null,
      expected,
      actual,
      new ArrayList<>()
    );
  }

  public static void containsInAnyOrder(@Nullable List<?> actual, @Nullable List<?> expected) {
    assertNotNull(expected);
    assertThat(actual, Matchers.containsInAnyOrder(expected.toArray()));
  }

  private void compareRecursively(@Nullable Class<?> rootActualType, @Nullable Field currentField, @Nullable Object expected, @Nullable Object actual, ArrayList<Field> pathParts) {
    if (tryCompareByInterceptors(rootActualType, currentField, expected, actual, pathParts))
      return;

    if (expected == null) {
      assertNull(
        actual,
        "Expected " + stringifyPath(pathParts) + " to be null" +
          " for root-object type " + rootActualType
      );
      return;
    }

    assertNotNull(actual, "Expected " + stringifyPath(pathParts) + " to not be null");

    // As of now, these types have to be handled externally...
    if (expected.getClass().isArray() || expected instanceof Collection<?> || expected instanceof Map<?,?>) {
      throw new IllegalStateException(
        "Encountered unhandled field of type " + expected.getClass().getSimpleName() +
        " at path " + stringifyPath(pathParts) +
        " for root-object of type " + rootActualType
      );
    }

    if (tryCompareDirectly(rootActualType, expected, actual, pathParts))
      return;

    var fields = getAllNonStaticFields(rootActualType, expected.getClass(), pathParts);

    if (fields.isEmpty()) {
      assertEquals(expected, actual);
      return;
    }

    for (var field : fields) {
      try {
        var expectedValue = field.get(expected);
        var actualValue = field.get(actual);

        pathParts.add(field);
        compareRecursively(rootActualType, field, expectedValue, actualValue, pathParts);

        if (!field.equals(pathParts.remove(pathParts.size() - 1)))
          throw new IllegalStateException("Expected " + field.getName() + " to be at the end of the path-part list");
      } catch (IllegalArgumentException | IllegalAccessException e) {
        throw new IllegalStateException(
          "Could not access field " + field.getName() +
          " at path " + stringifyPath(pathParts) +
          " for root-object of type " + rootActualType,
          e
        );
      }
    }
  }

  private boolean tryCompareDirectly(@Nullable Class<?> rootActualType, Object expected, Object actual, List<Field> pathParts) {
    var valueType = expected.getClass();

    if (isDirectlyComparable(valueType)) {
      if (!expected.equals(actual)) {
        throw new IllegalStateException(
          "Expected " + valueType.getSimpleName() + " " + expected +
          " at path " + stringifyPath(pathParts) +
          " but got " + actual + " instead" +
          " for root-object of type " + rootActualType
        );
      }

      return true;
    }

    return false;
  }

  private boolean isDirectlyComparable(Class<?> type) {
    return type.isEnum() || type == Class.class || Comparable.class.isAssignableFrom(type);
  }

  private boolean tryCompareByInterceptors(@Nullable Class<?> rootActualType, @Nullable Field field, Object expected, @Nullable Object actual, List<Field> pathParts) {
    Class<?> interceptorType;

    if (field == null) {
      // Case: Field is null, thus at root-call, expected is null, thus actual shall be null
      //       Cannot deduce type of interceptor, so handing back to caller
      if (expected == null)
        return false;

      interceptorType = expected.getClass();
    } else
      interceptorType = field.getType();

    var interceptor = interceptors.get(interceptorType);

    if (interceptor == null)
      return false;

    try {
      return interceptor.check(rootActualType, pathParts, expected, actual);
    } catch (Exception | AssertionFailedError e) {
      String message;

      if (e instanceof Exception exception)
        message = exception.getMessage();
      else {
        // Avoid IntelliJ printing diff-style mid-sentence by constructing the message manually
        AssertionFailedError assertionFailedError = (AssertionFailedError) e;

        var failureExpected = assertionFailedError.getExpected();
        var failureActual = assertionFailedError.getExpected();

        message = (
          "expected " + (failureExpected == null ? null : failureExpected.getStringRepresentation()) +
          " but got " + (failureActual == null ? null : failureActual.getStringRepresentation())
        );
      }

      throw new IllegalStateException(
        "An interceptor mismatched" +
        " at path " + stringifyPath(pathParts) +
        " with message \"" + message + "\"" +
        " for root-object of type " + rootActualType,
        e
      );
    }
  }

  private String stringifyPath(List<Field> pathParts) {
    var result = new StringJoiner(".");

    result.add("<root>");

    for (var pathPart : pathParts)
      result.add(pathPart.getName());

    return result.toString();
  }

  private List<Field> getAllNonStaticFields(@Nullable Class<?> rootActualType, Class<?> type, List<Field> pathParts) {
    return this.nonStaticFieldsCache.computeIfAbsent(type, k -> {
      var result = new ArrayList<Field>();
      var currentClass = type;

      do {
        for (var field : currentClass.getDeclaredFields()) {
          if (Modifier.isStatic(field.getModifiers()))
            continue;

          var fieldType = ClassUtils.primitiveToWrapper(field.getType());
          var isHandleable = false;

          if (interceptors.containsKey(fieldType)) {
            isHandleable = true;
          } else if (isDirectlyComparable(fieldType)) {
            isHandleable = true;
          } else if (Collection.class.isAssignableFrom(fieldType) || Map.class.isAssignableFrom(fieldType)) {
            isHandleable = true;
          } else if (expectedTypes.contains(fieldType)) {
            isHandleable = true;
          } else {
            var fieldTypeName = fieldType.getName();

            for (var expectedTypePrefix : this.expectedTypePrefixes) {
              if (!fieldTypeName.startsWith(expectedTypePrefix))
                continue;

              isHandleable = true;
              break;
            }
          }

          if (!isHandleable) {
            throw new IllegalStateException(
              "Encountered unexpected field type " + field.getType() +
              " on class " + currentClass +
              " at path " + stringifyPath(pathParts) +
              " for root-object of type " + rootActualType
            );
          }

          try {
            field.setAccessible(true);
          } catch (InaccessibleObjectException e) {
            throw new IllegalStateException(
              "Encountered inaccessible field type " + field.getType() +
              " on class " + currentClass +
              " at path " + stringifyPath(pathParts) +
              " for root-object of type " + rootActualType,
              e
            );
          }

          result.add(field);
        }
      } while ((currentClass = currentClass.getSuperclass()) != Object.class);

      return result;
    });
  }
}
