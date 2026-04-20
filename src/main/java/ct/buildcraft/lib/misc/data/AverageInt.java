/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.misc.data;

public class AverageInt {
    private int[] data;
    private int pos, precise;
    private int averageRaw, tickValue;

    public AverageInt(int precise) {
        this.precise = precise;
        clear();
    }

    public void clear() {
        this.data = new int[precise];
        this.pos = 0;
    }

    public double getAverage() {
        return (double) averageRaw / precise;
    }

    public void tick(int value) {
        internalTick(tickValue + value);
        tickValue = 0;
    }

    public void tick() {
        internalTick(tickValue);
        tickValue = 0;
    }

    private void internalTick(int value) {
        pos = ++pos % precise;
        int oldValue = data[pos];
        data[pos] = value;
        if (pos == 0) {
            averageRaw = 0;
            for (int iValue : data) {
                averageRaw += iValue;
            }
        } else {
            averageRaw = averageRaw - oldValue + value;
        }
    }

    public void push(int value) {
        tickValue += value;
    }
}
