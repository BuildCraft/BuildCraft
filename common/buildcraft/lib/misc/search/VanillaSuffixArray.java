package buildcraft.lib.misc.search;

import net.minecraft.client.searchtree.SuffixArray;
import net.minecraft.network.chat.Component;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.List;

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
//    public void add(T obj, String name)
    public void add(T obj, Component name) {
//        vanillaSuffixArray.add(obj, name);
        vanillaSuffixArray.add(obj, name.getString());
    }

    @Override
//    public void generate(Profiler prof)
    public void generate(ProfilerFiller prof) {
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
