package com.green.benjamin.diceGenerator;

import com.google.common.collect.Sets;

import java.util.Set;

public class DiceResult {
  private final String dieName;
  private final Set<Object> rollResults;

  public DiceResult(final String dieSides) {
    this.dieName = dieSides;
    this.rollResults = Sets.newHashSet();
  }

  public DiceResult addRollResult(final Object result) {
    rollResults.add(result);
    return this;
  }

  public String getDieName() {
    return dieName;
  }

  public Set<Object> getRollResults() {
    return rollResults;
  }
}
