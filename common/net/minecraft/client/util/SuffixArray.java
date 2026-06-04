// STUB(R.Chen): SuffixArray moved to net.minecraft.client.search.SuffixArray in 1.20.
// This is a compile shim that delegates. TODO: update imports to net.minecraft.client.search.
package net.minecraft.client.util;

import java.util.ArrayList;
import java.util.List;

/** STUB compile shim — SuffixArray moved in 1.20. Replaces with a simple list-based search. */
public class SuffixArray<T> {
    private final List<T> objects = new ArrayList<>();
    private final List<String> names = new ArrayList<>();

    public void add(T object, String name) {
        objects.add(object);
        names.add(name.toLowerCase());
    }

    public void generate() {}

    public List<T> findAll(String query) {
        String q = query.toLowerCase();
        List<T> results = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            if (names.get(i).contains(q)) results.add(objects.get(i));
        }
        return results;
    }
}
