package com.green.benjamin.diceGenerator;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class DiceService {

  public List<DiceResult> rollDice(final Map<Integer, Integer> diceCountMap) {
    final List<DiceResult> results = Lists.newArrayList();

    diceCountMap.entrySet().forEach(entry -> {
      final DiceResult diceResult = new DiceResult("D" + entry.getKey());
      for (int count = 0; count < entry.getValue(); count++) {
        diceResult.addRollResult(rollDie(entry.getKey()));
      }
      results.add(diceResult);

    });
    return results;
  }

  private int rollDie(final int dieSides) {
    return new Random(dieSides).nextInt();
  }

  public DiceResult rollDie(final EnumeratedDie enumeratedDie) {
    return new DiceResult("customDie")
        .addRollResult(enumeratedDie.toEnumeratedValue(rollDie(enumeratedDie.getSides())));
  }
}
