package buildcraft.lib.registry;

import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.util.math.BlockPos;

import buildcraft.api.filler.IFilledTemplate;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.api.filler.IFillerRegistry;

import buildcraft.builders.snapshot.Snapshot;
import buildcraft.builders.snapshot.Template;

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

    @Override
    public IFilledTemplate createFilledTemplate(BlockPos pos, BlockPos size) {
        Template template = new Template();
        template.size = size;
        template.offset = pos;
        template.data = new BitSet(Snapshot.getDataSize(size));
        return template.getFilledTemplate();
    }
}
