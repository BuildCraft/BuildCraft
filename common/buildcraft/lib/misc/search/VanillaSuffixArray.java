package buildcraft.lib.misc.search;

import java.util.List;

import net.minecraft.client.util.SuffixArray;
import net.minecraft.profiler.Profiler;

/** An implementation of {@link ISuffixArray} that delegates to a vanilla minecraft {@link SuffixArray}. */
public final class VanillaSuffixArray<T> implements ISuffixArray<T> {
    private final SuffixArray<T> vanillaSuffixArray;

    public VanillaSuffixArray(SuffixArray<T> vanillaSuffixArray) {
        this.vanillaSuffixArray = vanillaSuffixArray;
    }

    public VanillaSuffixArray() {
        this(new SuffixArray<>());
    }

    @Override
    public void add(T obj, String name) {
        vanillaSuffixArray.add(obj, name);
    }

    @Override
    public void generate(Profiler prof) {
        vanillaSuffixArray.generate();
    }

    @Override
    public SearchResult<T> search(String substring, int maxResults) {
        List<T> list = vanillaSuffixArray.search(substring);
        if (list.size() > maxResults) {
            int count = list.size();
            list.subList(maxResults, list.size()).clear();
            return new SearchResult<>(list, count);
        }
        return new SearchResult<>(list);
    }
}
