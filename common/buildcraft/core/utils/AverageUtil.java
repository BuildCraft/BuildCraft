/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.utils;

public class AverageUtil {
	private double[] data;
	private int pos, precise;
	private double averageRaw, tickValue;

	public AverageUtil(int precise) {
		this.precise = precise;
		this.data = new double[precise];
		this.pos = 0;
	}

	public double getAverage() {
		return averageRaw / precise;
	}

	public void tick(double value) {
		internalTick(tickValue + value);
		tickValue = 0;
	}

	public void tick() {
		internalTick(tickValue);
		tickValue = 0;
	}

	private void internalTick(double value) {
		pos = ++pos % precise;
		double oldValue = data[pos];
		data[pos] = value;
		if (pos == 0) {
			averageRaw = 0;
			for (double iValue : data) {
				averageRaw += iValue;
			}
		} else {
			averageRaw = averageRaw - oldValue + value;
		}
	}

	public void push(double value) {
		tickValue += value;
	}
}
