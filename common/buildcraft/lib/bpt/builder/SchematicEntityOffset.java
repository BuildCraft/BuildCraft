package buildcraft.lib.bpt.builder;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.Vec3d;

import buildcraft.api.bpt.BlueprintAPI;
import buildcraft.api.bpt.SchematicEntity;
import buildcraft.api.bpt.SchematicException;
import buildcraft.lib.misc.NBTUtils;

public class SchematicEntityOffset {
    public final SchematicEntity<?> schematic;
    public Vec3d vec;

    public SchematicEntityOffset(SchematicEntity<?> schematic, Vec3d vec) {
        this.schematic = schematic;
        this.vec = vec;
    }

    public SchematicEntityOffset(NBTTagCompound nbt) throws SchematicException {
        this.schematic = BlueprintAPI.deserializeSchematicEntity(nbt);
        this.vec = NBTUtils.readVec3d(nbt.getTag("vec"));
    }

    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();

        nbt.setTag("vec", NBTUtils.writeVec3d(vec));
        return nbt;
    }

    public void rotate(Rotation rotation) {
        // TODO translate the vec!
        schematic.rotate(rotation);
    }
}
