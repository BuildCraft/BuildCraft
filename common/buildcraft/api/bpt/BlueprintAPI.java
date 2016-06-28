package buildcraft.api.bpt;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;

//import buildcraft.api.IUniqueReader;

public class BlueprintAPI {
    private static final Map<ResourceLocation, SchematicFactoryWorldBlock> worldBlockFactories = new HashMap<>();
    private static final BiMap<Class<? extends Entity>, ResourceLocation> entityClassTypes = HashBiMap.create();
    private static final Map<ResourceLocation, SchematicFactoryWorldEntity<?>> worldEntityFactories = new HashMap<>();
    private static final Map<ResourceLocation, SchematicFactoryNBTBlock> nbtBlockFactories = new HashMap<>();
    private static final Map<ResourceLocation, SchematicFactoryNBTEntity> nbtEntityFactories = new HashMap<>();
    private static final Map<ResourceLocation, IBptTaskDeserializer> taskDeserializers = new HashMap<>();
    private static final Map<ResourceLocation, IBptReader<IBptAction>> actionDeserializers = new HashMap<>();

    public static void registerWorldBlockSchematic(Block block, SchematicFactoryWorldBlock factory) {
        worldBlockFactories.put(block.getRegistryName(), factory);
    }

    public static SchematicFactoryWorldBlock getWorldBlockSchematic(Block block) {
        ResourceLocation regName = block.getRegistryName();
        return worldBlockFactories.get(regName);
    }

    public static <E extends Entity> void registerWorldEntitySchematic(ResourceLocation location, Class<E> clazz, SchematicFactoryWorldEntity<E> factory) {
        entityClassTypes.put(clazz, location);
        worldEntityFactories.put(location, factory);
    }

    public static Class<? extends Entity> getWorldEntityClass(ResourceLocation location) {
        return entityClassTypes.inverse().get(location);
    }

    public static ResourceLocation getWorldEntityId(Class<? extends Entity> clazz) {
        return entityClassTypes.get(clazz);
    }

    public static <E extends Entity> SchematicFactoryWorldEntity<E> getWorldEntitySchematic(Class<E> clazz) {
        return (SchematicFactoryWorldEntity<E>) worldEntityFactories.get(clazz);
    }

    public static void registerNbtBlockSchematic(Block block, SchematicFactoryNBTBlock schematic) {
        nbtBlockFactories.put(block.getRegistryName(), schematic);
    }

    public static SchematicFactoryNBTBlock getNbtBlockSchematic(Block block) {
        ResourceLocation regName = block.getRegistryName();
        return nbtBlockFactories.get(regName);
    }

    public static void registerNbtEntitySchematic(ResourceLocation location, SchematicFactoryNBTEntity factory) {
        nbtEntityFactories.put(location, factory);
    }

    public static SchematicFactoryNBTEntity getNbtEntitySchematic(Class<? extends Entity> clazz) {
        return nbtEntityFactories.get(clazz);
    }

    public static void registerTaskDeserializer(ResourceLocation identifier, IBptTaskDeserializer deserializer) {
        taskDeserializers.put(identifier, deserializer);
    }

    public static IBptTaskDeserializer getTaskDeserializer(ResourceLocation identifier) {
        return taskDeserializers.get(identifier);
    }

    public static void registerActionDeserializer(ResourceLocation identifier, IBptReader<IBptAction> deserializer) {
        actionDeserializers.put(identifier, deserializer);
    }

    public static IBptReader<IBptAction> getActionDeserializer(ResourceLocation identifier) {
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
        IBptReader<IBptAction> reader = getActionDeserializer(new ResourceLocation(id));
        if (reader != null) {
//            return reader.deserialize(data); // FIXME: Anton
            return null; // FIXME: Anton
        } else {
            return null;
        }
    }

    public static SchematicBlock deserializeSchematicBlock(NBTTagCompound nbt) throws SchematicException {
        String regName = nbt.getString("block");
        if (StringUtils.isNullOrEmpty(regName)) return null;
        ResourceLocation loc = new ResourceLocation(regName);
        Block block = Block.REGISTRY.getObject(loc);
        SchematicFactoryNBTBlock des = getNbtBlockSchematic(block);
        if (des == null) {
            return null;
        } else {
            return des.createFromNBT(nbt);
        }
    }

    public static SchematicEntity<?> deserializeSchematicEntity(NBTTagCompound nbt) throws SchematicException {
        String regName = nbt.getString(SchematicEntity.NBT_KEY_ID);
        if (StringUtils.isNullOrEmpty(regName)) return null;
        SchematicFactoryNBTEntity factory = BlueprintAPI.getNbtEntitySchematic(BlueprintAPI.getWorldEntityClass(new ResourceLocation(regName)));
        if (factory == null) {
            return null;
        } else {
            return factory.createFromNBT(nbt);
        }
    }
}
