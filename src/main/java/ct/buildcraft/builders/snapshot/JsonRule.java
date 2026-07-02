/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.builders.snapshot;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

@SuppressWarnings("WeakerAccess")
public class JsonRule {
    public List<JsonSelector> selectors = null;
    public List<RequiredExtractor> requiredExtractors = null;
    public List<BlockPos> requiredBlockOffsets = null;
    public List<String> ignoredProperties = null;
    public List<BlockPos> updateBlockOffsets = null;
    public String placeBlock = null;
    public List<String> canBeReplacedWithBlocks = null;
    public CompoundTag replaceNbt = null;
    public boolean ignore = false; // blacklist for blocks
    public boolean capture = false; // whitelist for entities
}
