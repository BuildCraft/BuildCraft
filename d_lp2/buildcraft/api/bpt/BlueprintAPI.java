package buildcraft.api.bpt;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.ResourceLocation;

import buildcraft.api.IUniqueReader;
import buildcraft.lib.bpt.helper.BptActionPartiallyBreakBlock;
import buildcraft.lib.bpt.helper.BptActionSetBlockState;
import buildcraft.lib.bpt.helper.BptTaskBlockClear;
import buildcraft.lib.bpt.helper.BptTaskBlockStandalone;

public class BlueprintAPI {
    private static final Map<ResourceLocation, IBptTaskDeserializer> taskDeserializers = new HashMap<>();
    private static final Map<ResourceLocation, IUniqueReader<IBptAction>> actionDeserializers = new HashMap<>();

    public static void registerTaskDeserializer(ResourceLocation identifier, IBptTaskDeserializer deserializer) {
        taskDeserializers.put(identifier, deserializer);
    }

    public static IBptTaskDeserializer getTaskDeserializer(ResourceLocation identifier) {
        return taskDeserializers.get(identifier);
    }

    public static void registerActionDeserializer(ResourceLocation identifier, IUniqueReader<IBptAction> deserializer) {
        actionDeserializers.put(identifier, deserializer);
    }

    public static IUniqueReader<IBptAction> getActionDeserializer(ResourceLocation identifier) {
        return actionDeserializers.get(identifier);
    }

    static {
        // Default task deserializers
        registerTaskDeserializer(BptTaskBlockStandalone.ID, BptTaskBlockStandalone.Deserializer.INSTANCE);
        registerTaskDeserializer(BptTaskBlockClear.ID, BptTaskBlockClear.Deserializer.INSTANCE);

        // Default action deserializers
        registerActionDeserializer(BptActionSetBlockState.ID, BptActionSetBlockState.Deserializer.INSTANCE);
        registerActionDeserializer(BptActionPartiallyBreakBlock.ID, BptActionPartiallyBreakBlock.Deserializer.INSTANCE);
    }
}
