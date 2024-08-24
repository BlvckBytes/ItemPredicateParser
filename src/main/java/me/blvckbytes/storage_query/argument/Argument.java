package me.blvckbytes.storage_query.argument;

public interface Argument {
  /**
   * Zero-based index of the beginning argument (in case of multi-arg strings) within
   * the command as dispatched by the user
   */
  int getCommandArgumentIndex();
}
