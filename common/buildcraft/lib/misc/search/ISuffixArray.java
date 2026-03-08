package buildcraft.lib.misc.search;

import net.minecraft.profiler.IProfiler;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

public interface ISuffixArray<T> {
    // void add(T obj, String name);
    void add(T obj, ITextComponent name);

    // void generate(Profiler prof);
    void generate(IProfiler prof);

    SearchResult<T> search(String substring, int maxResults);

    public static final class SearchResult<T> {
        public final List<T> results;
        public final int realResultCount;

        public SearchResult(List<T> results, int realResultCount) {
            this.results = results;
            this.realResultCount = realResultCount;
        }

        public SearchResult(List<T> results) {
            this(results, results.size());
        }

        public boolean hasAllResults() {
            return results.size() == realResultCount;
        }
    }
}
