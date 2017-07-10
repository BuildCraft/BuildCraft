/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class JsonRule {
    public List<String> selectors = null;
    public List<ItemStack> requiredItems = null;
    public boolean copyRequiredItemsFromDrops = false;
    public boolean doNotCopyRequiredItemsFromBreakBlockDrops = false;
    public String copyRequiredItemsCountFromProperty = null;
    public List<String> copyRequiredItemsFromItemHandlersOnSides = null;
    public List<BlockPos> requiredBlockOffsets = null;
    public List<String> ignoredProperties = null;
    public List<String> ignoredTags = null;
    public List<BlockPos> updateBlockOffsets = null;
    public String placeBlock = null;
    public List<String> canBeReplacedWithBlocks = null;
    public boolean ignore = false; // blacklist for blocks
    public boolean capture = false; // whitelist for entities
}
