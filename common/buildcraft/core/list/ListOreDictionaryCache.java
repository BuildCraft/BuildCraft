package buildcraft.core.list;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraftforge.oredict.OreDictionary;

public final class ListOreDictionaryCache {
	public static final ListOreDictionaryCache INSTANCE = new ListOreDictionaryCache();
	private static final String[] TYPE_KEYWORDS = {
			"Tiny", "Dense", "Small"
	};
	private final Map<String, Set<Integer>> namingCache = new HashMap<String, Set<Integer>>();
	private final Set<String> registeredNames = new HashSet<String>();

	private ListOreDictionaryCache() {

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

	public static String getType(String name) {
		// Rules for finding type:
		// - Split just before the last uppercase character found.
		int splitLocation = name.length() - 1;
		while (splitLocation >= 0) {
			if (Character.isUpperCase(name.codePointAt(splitLocation))) {
				break;
			} else {
				splitLocation--;
			}
		}
		return splitLocation >= 0 ? name.substring(0, splitLocation) : name; // No null - this handles things like "record".
	}

	public static String getMaterial(String name) {
		// Rules for finding material:
		// - For every uppercase character, check if the character is not in
		//   TYPE_KEYWORDS. This is used to skip things like "plate[DenseIron]"
		//   or "dust[TinyRedstone]". That part should be the material still.
		int splitLocation = 0;
		String t = null;
		while (splitLocation < name.length()) {
			if (!Character.isUpperCase(name.codePointAt(splitLocation))) {
				splitLocation++;
			} else {
				t = name.substring(splitLocation);
				for (String s : TYPE_KEYWORDS) {
					if (t.startsWith(s)) {
						t = null;
						break;
					}
				}
				if (t != null) {
					break;
				} else {
					splitLocation++;
				}
			}
		}
		return splitLocation < name.length() ? t : null;
	}

	public void registerName(String name) {
		if (registeredNames.contains(name)) {
			return;
		}

		int oreID = OreDictionary.getOreID(name);

		addToNamingCache(getType(name), oreID);
		addToNamingCache(getMaterial(name), oreID);

		registeredNames.add(name);
	}
}
