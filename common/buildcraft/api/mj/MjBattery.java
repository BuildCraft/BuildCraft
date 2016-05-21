package buildcraft.api.mj;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import net.minecraftforge.common.util.INBTSerializable;

public class MjBattery implements INBTSerializable<NBTTagCompound> {
    private final int capacity;
    private int milliJoules = 0;

    public MjBattery(int capacity) {
        this.capacity = capacity;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("stored", milliJoules);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        milliJoules = nbt.getInteger("stored");
    }

    public void addPower(int milliJoules) {
        this.milliJoules += milliJoules;
    }

    public int extractPower(int min, int max) {
        if (milliJoules < min) return 0;
        int extracting = Math.min(milliJoules, max);
        milliJoules -= extracting;
        return extracting;
    }

    public boolean isFull() {
        return milliJoules >= capacity;
    }

    public int getContained() {
        return milliJoules;
    }

    public int getCapacity() {
        return capacity;
    }

    public void tick(World world, BlockPos position) {
        tick(world, new Vec3d(position.getX() + 0.5, position.getY() + 0.5, position.getZ() + 0.5));
    }

    public void tick(World world, Vec3d position) {
        if (milliJoules > capacity) {
            int diff = milliJoules - capacity;
            int lost = MathHelper.ceiling_double_int(diff / 30.0);
            milliJoules -= lost;
            MjAPI.EFFECT_MANAGER.createPowerLossEffect(world, position, lost);
        }
    }
}
