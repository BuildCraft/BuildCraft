package buildcraft.lib.registry;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import buildcraft.api.filler.IFillerPattern;
import buildcraft.api.filler.IFillerRegistry;

public enum FillerRegistry implements IFillerRegistry {
    INSTANCE;

    private final Map<String, IFillerPattern> patterns = new HashMap<>();

    @Override
    public void addPattern(IFillerPattern pattern) {
        patterns.put(pattern.getUniqueTag(), pattern);
    }

    @Override
    @Nullable
    public IFillerPattern getPattern(String name) {
        return patterns.get(name);
    }

    @Override
    public Collection<IFillerPattern> getPatterns() {
        return patterns.values();
    }
}
