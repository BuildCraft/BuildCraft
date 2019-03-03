package buildcraft.lib.misc.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import net.minecraft.profiler.Profiler;

import buildcraft.api.core.BCLog;

import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectSortedMap;

public class SimpleSuffixArray<T> implements ISuffixArray<T> {

    private static final boolean ADD_IS_GENERATE = true;
    private static final boolean USE_AVL = true;
    private static final boolean SPLIT_WORDS = true;

    private final List<String> tempAddedNames = new ArrayList<>();
    private final List<T> tempAddedObjects = new ArrayList<>();
    private int maxLength = 0;

    // really inefficent implementation of a suffix lookup
    private final Object2ObjectSortedMap<String, List<T>> suffixArray;

    public SimpleSuffixArray() {
        if (USE_AVL) {
            suffixArray = new Object2ObjectAVLTreeMap<>();
        } else {
            suffixArray = new Object2ObjectRBTreeMap<>();
        }
    }

    @Override
    public void add(T obj, String name) {
        if (!ADD_IS_GENERATE) {
            tempAddedObjects.add(obj);
            tempAddedNames.add(name);
        } else {
            int end = name.length();
            for (int s = name.length() - 1; s >= 0; s--) {
                char c = name.charAt(s);
                if (c == '\n' || (SPLIT_WORDS && c == ' ')) {
                    // Skip over /n as it's impossible to search over a line boundary
                    end = s;
                    continue;
                }
                String suffix = name.substring(s, end);
                List<T> list = suffixArray.get(suffix);
                if (list == null) {
                    list = new ArrayList<>();
                    suffixArray.put(suffix, list);
                }
                maxLength = Math.max(maxLength, suffix.length());
                list.add(obj);
            }
        }
    }

    @Override
    public void generate(Profiler prof) {
        if (ADD_IS_GENERATE) {
            BCLog.logger.info("[lib.search] Max suffix length is " + maxLength);
            for (String suffix : suffixArray.keySet()) {
                if (suffix.length() == maxLength) {
                    BCLog.logger.info("[lib.search]   '" + suffix + "'");
                }
            }
            return;
        }
        for (int i = 0; i < tempAddedNames.size(); i++) {
            String name = tempAddedNames.get(i);
            T obj = tempAddedObjects.get(i);
            int end = name.length();
            for (int s = name.length() - 1; s >= 0; s--) {
                char c = name.charAt(s);
                if (c == '\n' || (SPLIT_WORDS && c == ' ')) {
                    // Skip over /n as it's impossible to search over a line boundary
                    end = s;
                    continue;
                }
                String suffix = name.substring(s, end);
                List<T> list = suffixArray.get(suffix);
                if (list == null) {
                    list = new ArrayList<>();
                    suffixArray.put(suffix, list);
                }
                maxLength = Math.max(maxLength, suffix.length());
                list.add(obj);
            }
        }
        BCLog.logger.info("[lib.search] Max suffix length is " + maxLength);
    }

    @Override
    public SearchResult<T> search(String substring, int maxResults) {

        List<T> entries = new ArrayList<>();

        boolean first = true;
        String[] array = SPLIT_WORDS ? substring.split(" ") : new String[] { substring };
        for (String s : array) {
            Collection<T> real = first ? entries : new HashSet<>();
            for (List<T> values : suffixArray.subMap(s, s + (char) -1).values()) {
                real.addAll(values);
            }
            if (!first) {
                entries.retainAll(real);
            }
            first = false;
        }

        int realResultCount;
        if (entries.size() > maxResults) {
            realResultCount = entries.size();
            entries.subList(maxResults, entries.size()).clear();
        } else {
            realResultCount = entries.size();
        }

        return new SearchResult<>(entries, realResultCount);
    }
}
