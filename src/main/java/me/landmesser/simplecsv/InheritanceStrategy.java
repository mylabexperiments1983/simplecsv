package me.landmesser.simplecsv;

public enum InheritanceStrategy {
  /**
   * no field inheritance. default
   */
  NONE,
  /**
   * add base members first, then inherited ones
   */
  BASE_FIRST,
  /**
   * add inherited members first, then the base ones
   */
  BASE_LAST
}
