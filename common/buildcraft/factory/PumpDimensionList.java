/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import net.minecraftforge.fluids.Fluid;

public class PumpDimensionList {

	private List<Entry> entries;

	public PumpDimensionList(String string) {

		entries = new LinkedList<Entry>();

		for (String entryString : string.trim().split(",")) {

			Entry e = new Entry();

			if (entryString.startsWith("+/")) {
				e.isWhitelist = true;
			} else if (entryString.startsWith("-/")) {
				e.isWhitelist = false;
			} else {
				throw new RuntimeException("Malformed pumping.controlList entry: " + entryString + " (must start with +/ or -/)");
			}

			String secondString = entryString.substring(2);
			int i = secondString.indexOf('/');

			if (i < 0) {
				throw new RuntimeException("Malformed pumping.controlList entry: " + secondString
						+ " (missing second /)");
			}

			String dimIDString = secondString.substring(0, i);

			if ("*".equals(dimIDString)) {
				e.matchAnyDim = true;
			} else {
				e.dimID = Integer.parseInt(dimIDString);
			}

			e.fluidName = secondString.substring(i + 1).toLowerCase(Locale.ENGLISH);

			if (e.fluidName.equals("*")) {
				e.matchAnyFluid = true;
			}

			entries.add(0, e);
		}

		entries = new ArrayList<Entry>(entries);
	}

	private class Entry {
		boolean isWhitelist;
		String fluidName;
		int dimID;
		boolean matchAnyFluid;
		boolean matchAnyDim;

		boolean matches(Fluid fluid, int dim) {
			if (!matchAnyFluid) {
				if (!fluid.getName().equals(fluidName)) {
					return false;
				}
			}

			if (!matchAnyDim && dimID != dim) {
				return false;
			}

			return true;
		}
	}

	public boolean isFluidAllowed(Fluid fluid, int dim) {
		for (Entry e : entries) {
			if (e.matches(fluid, dim)) {
				return e.isWhitelist;
			}
		}
		return false;
	}


}
