package buildcraft.api.bpt;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.ResourceLocation;

public class BlueprintAPI {
    private static final Map<ResourceLocation, IBptActionDeserializer> deserializers = new HashMap<>();

    public static void registerDeserializer(ResourceLocation identifier, IBptActionDeserializer deserializer) {
        deserializers.put(identifier, deserializer);
    }

    public static IBptActionDeserializer getDeserializer(ResourceLocation identifier) {
        return deserializers.get(identifier);
    }
}
