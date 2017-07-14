package com.green.benjamin.diceGenerator;

import com.google.common.collect.Sets;

import java.util.Set;

public class DiceResult {
    private final int dieSides;
    private final Set<Integer> rollResults;

    public DiceResult(final int dieSides) {
        this.dieSides = dieSides;
        this.rollResults = Sets.newHashSet();
    }

    public void addRollResult(final int result) {
        rollResults.add(result);
    }

    public int getDieSides() {
        return dieSides;
    }

    public Set<Integer> getRollResults() {
        return rollResults;
    }
}
