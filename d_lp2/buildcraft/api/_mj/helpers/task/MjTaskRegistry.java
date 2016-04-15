package buildcraft.api._mj.helpers.task;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;

import buildcraft.api._mj.helpers.MjTaskManager;

public enum MjTaskRegistry {
    INSTANCE;

    private final Map<ResourceLocation, ISimpleMjTaskDeserializer> readerMap = new HashMap<>();

    public ISimpleMjTaskDeserializer getReaderFor(String id) {
        if (StringUtils.isNullOrEmpty(id)) return null;
        return getReaderFor(new ResourceLocation(id));
    }

    public ISimpleMjTaskDeserializer getReaderFor(ResourceLocation id) {
        return readerMap.get(id);
    }

    public interface ISimpleMjTaskDeserializer {
        IMjTask deserialize(NBTTagCompound nbt, MjTaskManager manager);
    }
}
