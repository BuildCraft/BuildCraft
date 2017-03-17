package buildcraft.builders.snapshot;

import java.util.List;
import java.util.Map;

public class JsonRule {
    public String name = null;
    public List<String> parentNames = null;
    public List<String> blocks = null;
    public List<String> requiredItems = null;
//    public boolean copyRequiredItemMetaFromBlock = false;
    public boolean copyRequiredItemsFromDrops = false;
    public String copyRequiredItemsCountFromProperty = null;
//    public String copyRequiredItemMetaFromProperty = null;
    public List<int[]> requiredBlockOffsets = null;
    public String copyOppositeRequiredBlockOffsetFromProperty = null;
    public boolean copyRequiredBlockOffsetsFromProperties = false;
    public List<String> ignoredProperties = null;
    public Map<String, String> freeIfHavePropertiesValues = null;
    public String placeBlock = null;
    public List<String> canBeReplacedWithBlocks = null;
    public boolean ignore = false;
}
