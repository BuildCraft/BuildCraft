package ct.buildcraft.api.mj;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

/** Provides a basic implementation of a simple battery. Note that you should call {@link #tick(Level, BlockPos)} or
 * {@link #tick(Level, Vec3)} every tick to allow for losing excess power. */
public class MjBattery implements INBTSerializable<CompoundTag> {
    private final long capacity;
    private long microJoules = 0;

    public MjBattery(long capacity) {
        this.capacity = capacity;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putLong("stored", microJoules);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        microJoules = nbt.getLong("stored");
    }

    public void writeToBuffer(ByteBuf buffer) {
        buffer.writeLong(microJoules);
    }

    public void readFromBuffer(ByteBuf buffer) {
        microJoules = buffer.readLong();
    }

    public long addPower(long microJoulesToAdd, FluidAction simulate) {
        if (simulate == FluidAction.EXECUTE) {
            this.microJoules += microJoulesToAdd;
        }
        return 0;
    }

    /** Attempts to add power, but only if this is not already full.
     * 
     * @param microJoulesToAdd The power to add.
     * @return The excess power. */
    public long addPowerChecking(long microJoulesToAdd, FluidAction simulate) {
        if (isFull()) {
            return microJoulesToAdd;
        } else {
            return addPower(microJoulesToAdd, simulate);
        }
    }

    public long extractAll() {
        return extractPower(0, microJoules);
    }

    /** Attempts to extract exactly the given amount of power.
     * 
     * @param power The amount of power to extract.
     * @return True if the power was removed, false if not. */
    public boolean extractPower(long power) {
        return extractPower(power, power) > 0;
    }

    public long extractPower(long min, long max) {
        if (microJoules < min) return 0;
        long extracting = Math.min(microJoules, max);
        microJoules -= extracting;
        return extracting;
    }

    public boolean isFull() {
        return microJoules >= capacity;
    }

    public long getStored() {
        return microJoules;
    }

    public long getCapacity() {
        return capacity;
    }

    public void tick(Level world, BlockPos position) {
        tick(world, new Vec3(position.getX() + 0.5, position.getY() + 0.5, position.getZ() + 0.5));
    }

    public void tick(Level world, Vec3 position) {
        if (microJoules > capacity * 2) {
            losePower(world, position);
        }
    }

    protected void losePower(Level world, Vec3 position) {
        long diff = microJoules - capacity * 2;
        long lost = ceilDivide(diff, 32);
        microJoules -= lost;
        MjAPI.EFFECT_MANAGER.createPowerLossEffect(world, position, lost);
    }

    private static long ceilDivide(long val, long by) {
        return (val / by) + (val % by == 0 ? 0 : 1);
    }

    public String getDebugString() {
        return MjAPI.formatMj(microJoules) + " / " + MjAPI.formatMj(capacity) + " MJ";
    }
}
