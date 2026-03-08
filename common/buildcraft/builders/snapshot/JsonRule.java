/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;

import java.util.List;

@SuppressWarnings("WeakerAccess")
public class JsonRule {
    public List<JsonSelector> selectors = null;
    public List<RequiredExtractor> requiredExtractors = null;
    public List<BlockPos> requiredBlockOffsets = null;
    public List<String> ignoredProperties = null;
    public List<BlockPos> updateBlockOffsets = null;
    public String placeBlock = null;
    public List<String> canBeReplacedWithBlocks = null;
    public CompoundNBT replaceNbt = null;
    public boolean ignore = false; // blacklist for blocks
    public boolean capture = false; // whitelist for entities
}
