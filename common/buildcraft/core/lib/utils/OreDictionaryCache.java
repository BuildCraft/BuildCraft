package buildcraft.core.lib.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;

public final class OreDictionaryCache {
	public static final OreDictionaryCache INSTANCE = new OreDictionaryCache();

	private final Map<String, Set<Integer>> namingCache = new HashMap<String, Set<Integer>>();
	private final Set<String> registeredNames = new HashSet<String>();

	private OreDictionaryCache() {

	}

	public Set<Integer> getListOfPartialMatches(String part) {
		return namingCache.get(part);
	}

	private void addToNamingCache(String s, int id) {
		if (s == null) {
			return;
		}

		Set<Integer> ll = namingCache.get(s);

		if (ll == null) {
			ll = new HashSet<Integer>();
			ll.add(id);
			namingCache.put(s, ll);
		} else {
			ll.add(id);
		}
	}

	private static int getSplitLocation(String name) {
		int splitLocation = 0;
		while (splitLocation < name.length()) {
			if (!Character.isUpperCase(name.codePointAt(splitLocation))) {
				splitLocation++;
			} else {
				break;
			}
		}

		return splitLocation;
	}

	public static String getFirstHalf(String name) {
		int splitLocation = getSplitLocation(name);
		return splitLocation < name.length() ? name.substring(0, splitLocation) : name; // No null - this handles things like "record".
	}

	public static String getSecondHalf(String name) {
		int splitLocation = getSplitLocation(name);
		return splitLocation < name.length() ? name.substring(splitLocation) : null;
	}

	@SubscribeEvent
	public void oreRegister(OreDictionary.OreRegisterEvent event) {
		registerName(event.Name);
	}

	public void registerName(String name) {
		if (registeredNames.contains(name)) {
			return;
		}

		int oreID = OreDictionary.getOreID(name);

		addToNamingCache(getFirstHalf(name), oreID);
		addToNamingCache(getSecondHalf(name), oreID);

		registeredNames.add(name);
	}
}
