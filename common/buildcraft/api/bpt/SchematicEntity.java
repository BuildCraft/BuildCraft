package buildcraft.api.bpt;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public abstract class SchematicEntity<E extends Entity> extends Schematic {
    public static final String NBT_KEY_ID = "entity";

    private final ResourceLocation id;

    public SchematicEntity(E entity) throws SchematicException {
        id = getId(entity.getClass());
    }

    public SchematicEntity(NBTTagCompound nbt, Class<E> clazz) throws SchematicException {
        id = getId(clazz);
        String read = nbt.getString(NBT_KEY_ID);
        if (!read.equals(id.toString())) {
            throw new SchematicException("Mismatched ID's (read " + read + ", expected " + id + ")");
        }
    }

    private static ResourceLocation getId(Class<? extends Entity> clazz) throws SchematicException {
        ResourceLocation id = BlueprintAPI.getWorldEntityId(clazz);
        if (id == null) {
            throw new SchematicException("Unknown entity class " + clazz + "!");
        }
        return id;
    }

    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString(NBT_KEY_ID, id.toString());
        return nbt;
    }
}
