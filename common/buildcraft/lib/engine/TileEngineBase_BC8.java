package buildcraft.lib.engine;

import java.util.Arrays;

import javax.annotation.Nonnull;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import buildcraft.api.enums.EnumEnergyStage;
import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.core.lib.BlockTileCache;
import buildcraft.lib.tile.TileBC_Neptune;

public abstract class TileEngineBase_BC8 extends TileBC_Neptune implements ITickable {
    /* BLUE, GREEN, YELLOW, RED, OVERHEAT, BLACK */
    private static final int[] PULSE_FREQUENCIES = { 60, 45, 35, 25, 15, 50 };

    @Nonnull
    public final IMjConnector conductor = createConnector();

    private EnumFacing currentDirection = EnumFacing.UP;
    // Keep a buffer of what tiles are infront of us.
    protected final BlockTileCache[] infrontBuffer = new BlockTileCache[getMaxEngineCarryDist() + 1];
    // refreshed from above, but is guaranteed to be non-null and contain non-null.
    protected TileEngineBase_BC8[] enginesInFront = new TileEngineBase_BC8[0];
    protected IMjReceiver receiverBuffer = null;
    private long microJoulesHeld;
    private float pulseStage = 0;

    public TileEngineBase_BC8() {
        // Just make sure
        remakeTileCaches();
    }

    //
    // @Override
    // public void readFromNBT(NBTTagCompound nbt) {
    // currentDirection = NBTUtils.readEnum(nbt.getTag("direction"), EnumFacing.class);
    // milliJoulesHeld = nbt.getInteger("milliJoulesHeld");
    // pulseStage = nbt.getFloat("pulseStage");
    // }
    //
    // @Override
    // public NBTTagCompound writeToNBT(int stage) {
    // NBTTagCompound nbt = new NBTTagCompound();
    // nbt.setTag("direction", NBTUtils.writeEnum(currentDirection));
    // nbt.setInteger("milliJoulesHeld", milliJoulesHeld);
    // nbt.setFloat("pulseStage", pulseStage);
    // return nbt;
    // }

    @Override
    public void update() {
        if (cannotUpdate()) return;
        // Refresh our engine caches. Every tick for some reason.
        // Except that this is really cheap to do, and we don't want to leave straggling tile entities.
        int num = 0;
        TileEngineBase_BC8[] engines = new TileEngineBase_BC8[infrontBuffer.length];
        for (BlockTileCache cache : infrontBuffer) {
            if (cache == null) break;
            // if the cache is not loaded then don't even bother checking.
            if (!cache.exists()) break;
            TileEntity tile = cache.getTile();
            if (tile instanceof TileEngineBase_BC8) {
                TileEngineBase_BC8 forwardEngine = (TileEngineBase_BC8) tile;
                // No corners
                if (forwardEngine.getCurrentDirection() != currentDirection) break;
                // Just make sure we can carry over- we don't want to carry power over a redstone engine.
                if (canCarryOver(forwardEngine) && forwardEngine.canCarryOver(this)) {
                    engines[num++] = forwardEngine;
                } else break;
            } else {
                IMjReceiver c = tile.getCapability(MjAPI.CAP_RECEIVER, currentDirection.getOpposite());
                if (c != null && c.canConnect(conductor) && conductor.canConnect(c)) {
                    receiverBuffer = c;
                }
                break;
            }
        }
        enginesInFront = Arrays.copyOf(engines, num);

        // Move onto the next stage of our pulse.
        pulseStage += 1 / getPulseFrequency();
        if (pulseStage >= 1) {
            pulseStage--;
        }
        if (pulseStage > 0.8) {
            float multiplier = 1 - pulseStage;
            int power = MathHelper.floor_float(multiplier * microJoulesHeld);
            microJoulesHeld -= power;
            sendPower(power);
        }
    }

    private void remakeTileCaches() {
        if (cannotUpdate()) return;
        BlockPos pos = getPos();
        for (int i = infrontBuffer.length - 1; i >= 0; i--) {
            pos = pos.offset(currentDirection);
            infrontBuffer[i] = new BlockTileCache(getWorld(), pos, false);
        }
    }

    protected void sendPower(long power) {
        if (receiverBuffer == null || !receiverBuffer.receivePower(power, false)) {
            MjAPI.EFFECT_MANAGER.createPowerLossEffect(getWorld(), new Vec3d(getPos()), currentDirection, power);
        }
    }

    public EnumFacing getCurrentDirection() {
        return currentDirection;
    }

    public boolean hasRedstoneSignal() {
        return worldObj.isBlockPowered(getPos());
    }

    protected void addPower(long microJoules) {
        microJoulesHeld += microJoules;
    }

    /** Creates a connector that uses this engine. You are encouraged to use {@link EngineConnector} */
    @Nonnull
    protected abstract IMjConnector createConnector();

    public abstract EnumEnergyStage getEnergyStage();

    /** @return The frequency of the power pulse, in ticks. */
    public int getPulseFrequency() {
        return PULSE_FREQUENCIES[getEnergyStage().ordinal()];
    }

    /** @return How many engines this engine can carry its power output over. This only carries over engines infront
     *         that are facing the same direction. */
    public abstract int getMaxEngineCarryDist();

    /** Checks to see if this can carry power through the given engine, or can carry power from the given engine. */
    protected abstract boolean canCarryOver(TileEngineBase_BC8 engine);

}
