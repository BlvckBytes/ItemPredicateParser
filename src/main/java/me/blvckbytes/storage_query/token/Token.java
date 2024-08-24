package me.blvckbytes.storage_query.token;

public interface Token {
  /**
   * Zero-based index of the beginning argument (in case of multi-arg strings) within
   * the command as dispatched by the user
   */
  int getCommandArgumentIndex();
}
