package buildcraft.builders.snapshot;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class JsonRule {
    public List<String> selectors = null;
    public List<ItemStack> requiredItems = null;
    public boolean copyRequiredItemsFromDrops = false;
    public boolean doNotCopyRequiredItemsFromBreakBlockDrops = false;
    public String copyRequiredItemsCountFromProperty = null;
    public List<String> copyRequiredItemsFromItemHandlersOnSides = null;
    public List<BlockPos> requiredBlockOffsets = null;
    public String copyOppositeRequiredBlockOffsetFromProperty = null;
    public boolean copyRequiredBlockOffsetsFromProperties = false;
    public List<String> ignoredProperties = null;
    public List<String> ignoredTags = null;
    public List<BlockPos> updateBlockOffsets = null;
    public String placeBlock = null;
    public List<String> canBeReplacedWithBlocks = null;
    public boolean ignore = false; // blacklist for blocks
    public boolean capture = false; // whitelist for entities
}
