package com.green.benjamin.exeptions;

public class InvalidDieSidesException extends Exception {


  public InvalidDieSidesException(final int sides,
                                  final String context,
                                  final int minRollsAndSides,
                                  final int maxSides) {

    super("Incorrect amount of " + context + "die sides " + sides
        + " must be between " + minRollsAndSides + " and " + maxSides + ".");
  }
}
