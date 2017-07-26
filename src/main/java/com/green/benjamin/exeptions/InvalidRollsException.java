package com.green.benjamin.exeptions;

public class InvalidRollsException extends Exception {

  public InvalidRollsException(final int rolls,
                               final int minRolls,
                               final int maxRolls) {

    super("Incorrect amount rolls per die requested " + rolls
        + " must be between " + minRolls + " and " + maxRolls + ".");
  }

}
