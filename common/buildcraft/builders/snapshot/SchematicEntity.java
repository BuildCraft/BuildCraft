package buildcraft.builders.snapshot;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.Fluid;

import java.util.ArrayList;
import java.util.List;

public class SchematicEntity implements INBTSerializable<NBTTagCompound> {
    public NBTTagCompound entityNbt;
    public Vec3d pos;
    public BlockPos blockPos;
    public List<ItemStack> requiredItems = new ArrayList<>();
    public List<Fluid> requiredFluids = new ArrayList<>();

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {

    }

    public SchematicEntity getRotated(Rotation rotation) {
        return this;
    }


    @SuppressWarnings("Duplicates")
    public boolean build(World world, BlockPos basePos) {
        return true;
    }


    @SuppressWarnings("Duplicates")
    public boolean buildWithoutChecks(World world, BlockPos basePos) {
        return build(world, basePos);
    }
}
