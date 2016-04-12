package buildcraft.api.bpt;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.ResourceLocation;

import buildcraft.api.IUniqueReader;

public enum BptActionRegistry {
    INSTANCE;

    private Map<ResourceLocation, IUniqueReader<IBptAction>> readerMap = new HashMap<>();
}
