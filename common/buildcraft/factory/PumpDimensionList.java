package buildcraft.factory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import net.minecraftforge.fluids.Fluid;

public class PumpDimensionList {
	public PumpDimensionList(String string) {

		entries = new LinkedList<Entry>();

		for(String entryString : string.trim().split(",")) {

			Entry e = new Entry();

			if(entryString.startsWith("+/")) {
				e.isWhitelist = true;
			} else if(entryString.startsWith("-/")) {
				e.isWhitelist = false;
			} else
				throw new RuntimeException("Malformed pumping.controlList entry: "+entryString+" (must start with +/ or -/)");

			entryString = entryString.substring(2);
			int i = entryString.indexOf('/');

			if(i < 0)
				throw new RuntimeException("Malformed pumping.controlList entry: "+entryString+" (missing second /)");

			String dimIDString = entryString.substring(0, i);

			if(dimIDString.equals("*"))
				e.matchAnyDim = true;
			else
				e.dimID = Integer.parseInt(dimIDString);

			e.fluidName = entryString.substring(i + 1).toLowerCase(Locale.ENGLISH);
			if(e.fluidName.equals("*"))
				e.matchAnyFluid = true;

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
			if(!matchAnyFluid) {
				if(!fluid.getName().equals(fluidName))
					return false;
			}
			if(!matchAnyDim && dimID != dim)
				return false;
			return true;
		}
	}

	private List<Entry> entries;

	public boolean isFluidAllowed(Fluid fluid, int dim) {
		for(Entry e : entries)
			if(e.matches(fluid, dim))
				return e.isWhitelist;
		return false;
	}


}
