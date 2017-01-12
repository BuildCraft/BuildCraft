package buildcraft.core.marker.volume;

import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public enum AddonsRegistry {
    INSTANCE;

    private final Map<ResourceLocation, Class<? extends Addon>> addonClasses = new HashMap<>();

    public void register(ResourceLocation name, Class<? extends Addon> clazz) {
        if (!addonClasses.containsKey(name)) {
            addonClasses.put(name, clazz);
        }
    }

    public Class<? extends Addon> getClassByName(ResourceLocation name) {
        return addonClasses.get(name);
    }

    public ResourceLocation getNameByClass(Class<? extends Addon> clazz) {
        return addonClasses.entrySet().stream().filter(nameClass -> nameClass.getValue().equals(clazz)).findFirst().orElse(null).getKey();
    }
}
