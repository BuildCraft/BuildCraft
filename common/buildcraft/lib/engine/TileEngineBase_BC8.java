package buildcraft.lib.engine;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.enums.EnumEnergyStage;
import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.tiles.IDebuggable;

import buildcraft.lib.block.VanillaRotationHandlers;
import buildcraft.lib.misc.ParticleUtil;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;

// FIXME: This needs reverting to (close to) earlier engine code -- this is all horrible.
public abstract class TileEngineBase_BC8 extends TileBC_Neptune implements ITickable, IDebuggable {
    /** The starting temperature of all engines */
    public static final int TEMP_START = 19;
    /** The temperature of all (normal) burning fuel. */
    public static final int TEMP_ENGINE_ENERGY = 500;
    /** The temperature of lost power, per milli MJ */
    public static final int TEMP_LOST_POWER = 12;

    public static final double TEMP_CHANGE_HEAT = 0.00125;
    public static final double TEMP_CHANGE_AIR = 0.0125;
    public static final double TEMP_CHANGE_WATER = 0.025;

    /* BLUE, GREEN, YELLOW, RED, OVERHEAT, BLACK */
    private static final int[] PULSE_FREQUENCIES = { 80, 60, 45, 30, 25, 1000000 };

    @Nonnull
    public final IMjConnector conductor = createConnector();

    private EnumFacing currentDirection = EnumFacing.UP;
    // Keep a buffer of what tiles are infront of us.
    // protected final BlockTileCache[] infrontBuffer = new BlockTileCache[Math.abs(getMaxEngineCarryDist()) + 1];
    // refreshed from above, but is guaranteed to be non-null and contain non-null.
    protected TileEngineBase_BC8[] enginesInFront = new TileEngineBase_BC8[0];
    protected IMjReceiver receiverBuffer = null;

    // Various fields for keeping the engine running properly
    private long microJoulesHeld;
    private long milliTemp = TEMP_START * 1000;
    private double pulseStage = 0;
    private boolean isOn = false;
    private int meltdownTicks = 0;
    private boolean reachedMeltdown = false;

    public TileEngineBase_BC8() {}

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

    public EnumActionResult attemptRotation() {
        EnumFacing[] possible = VanillaRotationHandlers.getAllSidesArray();
        EnumFacing current = currentDirection;
        int ord = VanillaRotationHandlers.getOrdinal(current, possible);
        for (int i = 1; i < possible.length; i++) {
            int next = (ord + i) % possible.length;
            EnumFacing toTry = possible[next];
            if (true) {// TODO: replace with sided check
                currentDirection = toTry;
                makeTileCache();
                redrawBlock();
                return EnumActionResult.SUCCESS;
            }
        }
        return EnumActionResult.FAIL;
    }

    @Override
    public void update() {
        if (cannotUpdate() || worldObj.isRemote) return;

        // Check to see if we are still active
        isOn = worldObj.isBlockIndirectlyGettingPowered(getPos()) > 0 && hasFuelToBurn();

        // Apply air cooling
        // TODO: biome-based
        changeHeat(TEMP_START, TEMP_CHANGE_AIR);

        if (reachedMeltdown) {
            meltdownTicks--;
            if (meltdownTicks == 0) {
                reachedMeltdown = false;
            }
        }

        if (isActive()) {
            // Check to see if we have reached meltdown stage -- we are too hot

            if (getEnergyStage() == EnumEnergyStage.OVERHEAT) {
                meltdownTicks++;
                if (meltdownTicks >= 100) {
                    reachedMeltdown = true;
                    meltdownTicks = 100;
                }
            }

            makeTileCacheIfNeeded();

            // Refresh our engine and receiver cache. Every tick for some reason.
            // Except that this is really cheap to do, and we don't want to try
            // and inject power to a non-existent tile
            int num = 0;
            // TileEngineBase_BC8[] engines = new TileEngineBase_BC8[infrontBuffer.length];
            // for (BlockTileCache cache : infrontBuffer) {
            // if (cache == null) break;
            // // if the cache is not loaded then don't even bother checking.
            // if (!cache.exists()) break;
            // TileEntity tile = cache.getTile();
            // if (tile instanceof TileEngineBase_BC8) {
            // TileEngineBase_BC8 forwardEngine = (TileEngineBase_BC8) tile;
            // // No corners
            // if (forwardEngine.getCurrentDirection() != currentDirection) break;
            // // Just make sure we can carry over- we don't want to carry power over a redstone engine.
            // if (canCarryOver(forwardEngine) && forwardEngine.canCarryOver(this)) {
            // engines[num++] = forwardEngine;
            // } else break;
            // } else if (tile != null) {
            // IMjReceiver c = tile.getCapability(MjAPI.CAP_RECEIVER, currentDirection.getOpposite());
            // if (c != null && c.canConnect(conductor) && conductor.canConnect(c)) {
            // receiverBuffer = c;
            // }
            // break;
            // }
            // }
            // enginesInFront = Arrays.copyOf(engines, num);

            // Move onto the next stage of our pulse.
            pulseStage += 1 / (double) getPulseFrequency();
            if (pulseStage >= 1) {
                pulseStage--;
            }
            if (pulseStage > 0.4 && pulseStage < 0.6) {
                double multiplier = pulseStage * 1.8;// Get a number close to, or above 1
                long power = MathHelper.floor_double_long(multiplier * microJoulesHeld);
                if (power > microJoulesHeld) {
                    power = microJoulesHeld;
                }
                microJoulesHeld -= power;
                sendPower(power);
            }
        } else {

        }
    }

    public boolean isActive() {
        return isOn && !reachedMeltdown;
    }

    private void makeTileCacheIfNeeded() {
        // if (cannotUpdate() || infrontBuffer[0] != null) {
        // return;
        // }
        makeTileCache();
    }

    private void makeTileCache() {
        if (cannotUpdate()) {
            return;
        }
        // for (int i = 0; i < infrontBuffer.length; i++) {
        // infrontBuffer[i] = new BlockTileCache(getWorld(), getPos().offset(currentDirection, i + 1), false);
        // }
    }

    protected void sendPower(long power) {
        long excess = power;
        if (receiverBuffer != null) {
            excess = receiverBuffer.receivePower(power, false);
        }
        if (excess <= 0) {
            return;
        }
        MjAPI.EFFECT_MANAGER.createPowerLossEffect(getWorld(), new Vec3d(getPos()), currentDirection, excess);
        ParticleUtil.showTempPower(getWorld(), getPos(), getCurrentDirection(), excess);
        // This is horrible!
        addHeatFromPower(excess);
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

    protected void addHeatFromPower(long microJoules) {
        int jouledHeatTarget = (int) (microJoules / 1000);
        jouledHeatTarget = jouledHeatTarget * TEMP_LOST_POWER;
        if (jouledHeatTarget > getTemperature()) {
            changeHeat(jouledHeatTarget, TEMP_CHANGE_HEAT);
        }
    }

    /** @param to The "ideal" heat value - generally the temperature of the object changing the temperature.
     * @param multiplier How quickly this change should happen over. A good value for air cooling is 0.002, and a good
     *            value for fluid cooling is 0.05. If this is 1 then this is effectively a setter for the
     *            temperature. */
    protected void changeHeat(int to, double multiplier) {
        int diff = to - getTemperature();
        if (diff != 0) {
            milliTemp += diff * multiplier * 1000;
        }
    }

    public int getTemperature() {
        return (int) (milliTemp / 1000);
    }

    // Overridable engine functions

    /** Creates a connector that uses this engine. You are encouraged to use {@link EngineConnector} */
    @Nonnull
    protected abstract IMjConnector createConnector();

    /** Checks to see if this engine can burn more fuel. This is only called if this is receiving a redstone signal. */
    protected abstract boolean hasFuelToBurn();

    public abstract EnumEnergyStage getEnergyStage();

    /** @return The frequency of the power pulse, in ticks. */
    public int getPulseFrequency() {
        return PULSE_FREQUENCIES[getEnergyStage().ordinal()];
    }

    /** @return How many engines this engine can carry its power output over. This only carries over engines infront
     *         that are facing the same direction. If this is a negative number then this WILL crash, or be ignored. */
    public abstract int getMaxEngineCarryDist();

    /** Checks to see if this can carry power through the given engine, or can carry power from the given engine. */
    protected abstract boolean canCarryOver(TileEngineBase_BC8 engine);

    // Debugging

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("");
        left.add("Stage = " + pulseStage);
        left.add("Held = " + MjAPI.formatMjShort(microJoulesHeld));
        left.add("Temp = " + milliTemp / 1000 + "°C");
    }

    // TODO: Adv debugging

    // Networking

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Side side) {
        super.writePayload(id, buffer, side);
        if (side == Side.SERVER) {
            if (id == NET_RENDER_DATA) {
                buffer.writeByte(currentDirection.getIndex());
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == Side.CLIENT) {
            if (id == NET_RENDER_DATA) {
                currentDirection = EnumFacing.getFront(buffer.readUnsignedByte());
            }
        }
    }
}
