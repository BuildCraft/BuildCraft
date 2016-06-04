package buildcraft.api.bpt;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;

import buildcraft.api.IUniqueReader;
import buildcraft.lib.bpt.helper.BptActionPartiallyBreakBlock;
import buildcraft.lib.bpt.helper.BptActionSetBlockState;
import buildcraft.lib.bpt.helper.BptTaskBlockClear;
import buildcraft.lib.bpt.helper.BptTaskBlockStandalone;

public class BlueprintAPI {
    private static final Map<ResourceLocation, SchematicFactoryWorldBlock> schematicFactories = new HashMap<>();
    private static final Map<ResourceLocation, SchematicFactoryNBTBlock> schematicBlockDeserializers = new HashMap<>();
    private static final Map<ResourceLocation, SchematicFactoryNBTEntity> schematicEntityDeserializers = new HashMap<>();
    private static final Map<ResourceLocation, IBptTaskDeserializer> taskDeserializers = new HashMap<>();
    private static final Map<ResourceLocation, IUniqueReader<IBptAction>> actionDeserializers = new HashMap<>();

    public static void registerWorldBlockSchematic(Block block, SchematicFactoryWorldBlock factory) {
        schematicFactories.put(block.getRegistryName(), factory);
    }

    public static void registerSchematicBlockDeserializer(Block block, SchematicFactoryNBTBlock schematic) {
        schematicBlockDeserializers.put(block.getRegistryName(), schematic);
    }

    public static SchematicFactoryWorldBlock getWorldFactoryFor(Block block) {
        ResourceLocation regName = block.getRegistryName();
        return schematicFactories.get(regName);
    }

    public static SchematicFactoryNBTBlock getNBTFactoryFor(Block block) {
        ResourceLocation regName = block.getRegistryName();
        return schematicBlockDeserializers.get(regName);
    }

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

    public static NBTTagCompound serializeAction(IBptAction action) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("id", action.getRegistryName().toString());
        nbt.setTag("data", action.serializeNBT());
        return nbt;
    }

    public static IBptAction deserializeAction(NBTTagCompound nbt) {
        String id = nbt.getString("id");
        NBTTagCompound data = nbt.getCompoundTag("data");
        IUniqueReader<IBptAction> reader = getActionDeserializer(new ResourceLocation(id));
        if (reader != null) {
            return reader.deserialize(data);
        } else {
            return null;
        }
    }

    static {
        // Default task deserializers
        registerTaskDeserializer(BptTaskBlockStandalone.ID, BptTaskBlockStandalone::new);
        registerTaskDeserializer(BptTaskBlockClear.ID, BptTaskBlockClear::new);

        // Default action deserializers
        registerActionDeserializer(BptActionSetBlockState.ID, BptActionSetBlockState.Deserializer.INSTANCE);
        registerActionDeserializer(BptActionPartiallyBreakBlock.ID, BptActionPartiallyBreakBlock.Deserializer.INSTANCE);
    }

    public static SchematicBlock deserializeSchematic(NBTTagCompound nbt) throws SchematicException {
        String regName = nbt.getString("block");
        if (StringUtils.isNullOrEmpty(regName)) return null;
        ResourceLocation loc = new ResourceLocation(regName);
        Block block = Block.REGISTRY.getObject(loc);
        SchematicFactoryNBTBlock des = getNBTFactoryFor(block);
        if (des == null) {
            return null;
        } else {
            return des.createFromNBT(nbt);
        }
    }
}
