/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.builders.registry;

import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import ct.buildcraft.api.filler.IFilledTemplate;
import ct.buildcraft.api.filler.IFillerPattern;
import ct.buildcraft.api.filler.IFillerRegistry;
import ct.buildcraft.builders.snapshot.Snapshot;
import ct.buildcraft.builders.snapshot.Template;
import net.minecraft.core.BlockPos;

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
