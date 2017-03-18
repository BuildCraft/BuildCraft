package buildcraft.builders.snapshot;

import java.util.List;

public class JsonRule {
    public List<String> selectors = null;
    public List<String> requiredItems = null;
    public boolean copyRequiredItemsFromDrops = false;
    public String copyRequiredItemsCountFromProperty = null;
    public List<String> copyRequiredItemsFromItemHandlersOnSides = null;
    public List<int[]> requiredBlockOffsets = null;
    public String copyOppositeRequiredBlockOffsetFromProperty = null;
    public boolean copyRequiredBlockOffsetsFromProperties = false;
    public List<String> ignoredProperties = null;
    public List<String> ignoredTags = null;
    public String placeBlock = null;
    public List<String> canBeReplacedWithBlocks = null;
    public boolean ignore = false;
}
