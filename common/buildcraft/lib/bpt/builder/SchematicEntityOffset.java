package buildcraft.lib.bpt.builder;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import buildcraft.api.bpt.BlueprintAPI;
import buildcraft.api.bpt.SchematicEntity;
import buildcraft.api.bpt.SchematicException;

import buildcraft.lib.misc.NBTUtils;
import buildcraft.lib.misc.PositionUtil;
import buildcraft.lib.misc.VecUtil;

public class SchematicEntityOffset {
    public final SchematicEntity<?> schematic;
    public Vec3d vec;

    public SchematicEntityOffset(SchematicEntity<?> schematic, Vec3d vec) {
        this.schematic = schematic;
        this.vec = vec;
    }

    public SchematicEntityOffset(NBTTagCompound nbt) throws SchematicException {
        this.schematic = BlueprintAPI.deserializeSchematicEntity(nbt.getCompoundTag("sch"));
        this.vec = NBTUtils.readVec3d(nbt.getTag("vec"));
    }

    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("sch", schematic.serializeNBT());
        nbt.setTag("vec", NBTUtils.writeVec3d(vec));
        return nbt;
    }

    public void rotate(Axis axis, Rotation rotation, BlockPos oldSize) {
        schematic.rotate(axis, rotation);
        Vec3d center = new Vec3d(oldSize);
        center = VecUtil.scale(center, 0.5);
        vec = vec.subtract(center);
        vec = PositionUtil.rotateVec(vec, axis, rotation);
        vec = vec.add(center);
    }

    public void mirror(Axis axis, BlockPos size) {
        schematic.mirror(axis);
        double value = VecUtil.getValue(size, axis) - 1 - VecUtil.getValue(vec, axis);
        vec = VecUtil.replaceValue(vec, axis, value);
    }
}
