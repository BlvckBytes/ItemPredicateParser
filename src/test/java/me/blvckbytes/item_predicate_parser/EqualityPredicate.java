package me.blvckbytes.item_predicate_parser;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.List;

@FunctionalInterface
public interface EqualityPredicate<T> {

  /**
   * @return True on pass, throw on failure, false on inability to handle
   */
  boolean check(
    @Nullable Class<?> rootActualType,
    List<Field> pathParts,
    @Nullable T expected,
    @Nullable T actual
  );
}
