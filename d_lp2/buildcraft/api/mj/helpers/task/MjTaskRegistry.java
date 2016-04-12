package buildcraft.api.mj.helpers.task;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.ResourceLocation;

import buildcraft.api.IUniqueReader;

public enum MjTaskRegistry {
    INSTANCE;

    private final Map<ResourceLocation, IUniqueReader<IMjTask>> readerMap = new HashMap<>();
    
    // TODO: Flesh out
}
