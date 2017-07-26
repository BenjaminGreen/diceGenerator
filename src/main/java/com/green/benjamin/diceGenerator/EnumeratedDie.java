package com.green.benjamin.diceGenerator;

import java.util.List;

public class EnumeratedDie {
  private final List<String> enumeratedValues;

  public EnumeratedDie(final List<String> enumeratedValues) {
    this.enumeratedValues = enumeratedValues;
  }


  public int getSides() {
    return enumeratedValues.size() + 1;
  }

  public String toEnumeratedValue(final int result) {
    return enumeratedValues.get(result - 1);
  }
}
