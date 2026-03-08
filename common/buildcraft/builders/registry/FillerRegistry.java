/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.registry;

import buildcraft.api.filler.IFilledTemplate;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.api.filler.IFillerRegistry;
import buildcraft.builders.snapshot.Snapshot;
import buildcraft.builders.snapshot.Template;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
