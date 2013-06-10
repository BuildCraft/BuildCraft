package buildcraft.factory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.minecraftforge.liquids.LiquidDictionary;
import net.minecraftforge.liquids.LiquidStack;

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
			
			e.liquidName = entryString.substring(i + 1);
			if(e.liquidName.equals("*"))
				e.matchAnyLiquid = true;
			
			entries.add(0, e);
		}
		
		entries = new ArrayList<Entry>(entries);
	}

	private class Entry {
		boolean isWhitelist;
		LiquidStack liquidStack;
		String liquidName;
		int dimID;
		boolean matchAnyLiquid;
		boolean matchAnyDim;
		
		private void initLiquidStack() {
			liquidStack = LiquidDictionary.getLiquid(liquidName, 1);
			if(liquidStack == null)
				throw new RuntimeException("Configuration error: unknown liquid "+liquidName+" in pumping.controlList");
		}
		
		boolean matches(LiquidStack liquid, int dim) {
			if(!matchAnyLiquid) {
				if(liquidStack == null)
					initLiquidStack();
				if(!liquidStack.isLiquidEqual(liquid))
					return false;
			}
			if(!matchAnyDim && dimID != dim)
				return false;
			return true;
		}
	}
	
	private List<Entry> entries;

	public boolean isLiquidAllowed(LiquidStack liquid, int dim) {
		for(Entry e : entries)
			if(e.matches(liquid, dim))
				return e.isWhitelist;
		return false;
	}
	
	
}
