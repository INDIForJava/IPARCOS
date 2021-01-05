package org.indilib.i4j.iparcos;

import android.view.View;

/**
 * @author Noman Rafique
 * @author marcocipriani01
 * @see <a href="https://stackoverflow.com/a/41466381">Continuously increase integer value as the button is pressed</a>
 */
public abstract class CounterHandler extends LongPressHandler {

    private final int steps;
    private final boolean isCycle;
    private int minValue;
    private int maxValue;
    private int currentValue;

    public CounterHandler(View incrementView, View decrementView, int minValue,
                          int maxValue, int initialValue, int steps,
                          long delay, boolean isCycle) {
        super(incrementView, decrementView, delay);
        if ((minValue != -1) && (maxValue != -1) && (minValue >= maxValue)) {
            throw new IllegalArgumentException("Counter bound error!");
        }
        this.minValue = minValue;
        this.maxValue = maxValue;
        if (((minValue != -1) && (initialValue < minValue)) || ((maxValue != -1) && (initialValue > maxValue))) {
            throw new IllegalArgumentException("Initial value out of bounds!");
        }
        this.currentValue = initialValue;
        this.steps = steps;
        this.isCycle = isCycle;
    }

    public int getValue() {
        return currentValue;
    }

    public void setValue(int newValue) {
        if ((maxValue != -1) && (newValue > maxValue)) {
            currentValue = maxValue;
        } else if ((minValue != -1) && (newValue < minValue)) {
            currentValue = minValue;
        } else {
            currentValue = newValue;
        }
    }

    public void setMaxValue(int maxValue) {
        if (maxValue <= minValue) {
            throw new IllegalArgumentException("Max value < min value!");
        }
        this.maxValue = maxValue;
        if (currentValue > this.maxValue) currentValue = this.maxValue;
    }

    public void setMinValue(int minValue) {
        if (minValue >= maxValue) throw new IllegalArgumentException("Min value > max value!");
        this.minValue = minValue;
        if (currentValue < this.minValue) currentValue = this.minValue;
    }

    @Override
    protected void onIncrement() {
        int number = this.currentValue;
        if (maxValue != -1) {
            if (number + steps <= maxValue) {
                number += steps;
            } else if (isCycle) {
                number = (minValue == -1) ? 0 : minValue;
            }
        } else {
            number += steps;
        }
        this.currentValue = number;
    }

    @Override
    protected void onDecrement() {
        int number = this.currentValue;
        if (minValue != -1) {
            if (number - steps >= minValue) {
                number -= steps;
            } else if (isCycle) {
                number = (maxValue == -1) ? 0 : maxValue;
            }
        } else {
            number -= steps;
        }
        this.currentValue = number;
    }
}