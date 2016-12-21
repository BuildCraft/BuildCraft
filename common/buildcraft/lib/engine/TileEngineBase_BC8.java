package buildcraft.lib.engine;

import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;

import buildcraft.api.enums.EnumPowerStage;
import buildcraft.api.mj.*;
import buildcraft.api.tiles.IDebuggable;

import buildcraft.lib.block.VanillaRotationHandlers;
import buildcraft.lib.tile.TileBC_Neptune;

public abstract class TileEngineBase_BC8 extends TileBC_Neptune implements ITickable, IDebuggable {

    /** Heat per {@link MjAPI#MJ}. */
    public static final double HEAT_PER_MJ = 0.0023;

    public static final double MIN_HEAT = 20;
    public static final double IDEAL_HEAT = 100;
    public static final double MAX_HEAT = 250;

    @Nonnull
    public final IMjConnector mjConnector = createConnector();
    private final MjCapabilityHelper mjCaps = new MjCapabilityHelper(mjConnector);

    protected double heat = MIN_HEAT;
    protected long power = 0;
    private long lastPower = 0;
    /** Increments from 0 to 1. Above 0.5 all of the held power is emitted. */
    private float progress;
    private int progressPart = 0;

    protected EnumPowerStage powerStage = EnumPowerStage.BLUE;
    protected EnumFacing currentDirection = EnumFacing.UP;

    public long currentOutput;
    public boolean isRedstonePowered = false;
    private boolean isOn = false;
    private boolean isPumping = false;

    public TileEngineBase_BC8() {}

    public EnumActionResult attemptRotation() {
        EnumFacing[] possible = VanillaRotationHandlers.getAllSidesArray();
        EnumFacing current = currentDirection;
        int ord = VanillaRotationHandlers.getOrdinal(current, possible);
        for (int i = 1; i < possible.length; i++) {
            int next = (ord + i) % possible.length;
            EnumFacing toTry = possible[next];
            if (true) {// TODO: replace with sided check
                currentDirection = toTry;
                // makeTileCache();
                redrawBlock();
                return EnumActionResult.SUCCESS;
            }
        }
        return EnumActionResult.FAIL;
    }

    public boolean onActivated(EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        return false;
    }

    public double getPowerLevel() {
        return power / (double) getMaxPower();
    }

    protected EnumPowerStage computeEnergyStage() {// TODO: RENAME
        double powerLevel = getHeatLevel();
        if (powerLevel < 0.25f) return EnumPowerStage.BLUE;
        else if (powerLevel < 0.5f) return EnumPowerStage.GREEN;
        else if (powerLevel < 0.75f) return EnumPowerStage.YELLOW;
        else if (powerLevel < 1f) return EnumPowerStage.RED;
        else return EnumPowerStage.OVERHEAT;
    }

    public final EnumPowerStage getEnergyStage() {// TODO: RENAME
        if (!worldObj.isRemote) {
            if (powerStage == EnumPowerStage.OVERHEAT) return powerStage;
            EnumPowerStage newStage = computeEnergyStage();

            if (powerStage != newStage) {
                powerStage = newStage;
                sendNetworkUpdate(NET_RENDER_DATA);
            }
        }

        return powerStage;
    }

    public void updateHeatLevel() {
        heat = ((MAX_HEAT - MIN_HEAT) * getPowerLevel()) + MIN_HEAT;
    }

    public double getHeatLevel() {
        return (heat - MIN_HEAT) / (MAX_HEAT - MIN_HEAT);
    }

    public double getIdealHeatLevel() {
        return heat / IDEAL_HEAT;
    }

    public double getHeat() {
        return heat;
    }

    public double getPistonSpeed() {
        if (!worldObj.isRemote) {
            return Math.max(0.16 * getHeatLevel(), 0.01);
        }

        switch (getEnergyStage()) {
            case BLUE:
                return 0.02;
            case GREEN:
                return 0.04;
            case YELLOW:
                return 0.08;
            case RED:
                return 0.16;
            default:
                return 0;
        }
    }

    @Nonnull
    protected abstract IMjConnector createConnector();

    @Override
    public void update() {
        deltaManager.tick();
        if (cannotUpdate()) return;

        if (worldObj.isRemote) {
            // TODO!
            return;
        }

        lastPower = 0;
        isRedstonePowered = worldObj.isBlockIndirectlyGettingPowered(getPos()) > 0;

        if (!isRedstonePowered) {
            if (power > MjAPI.MJ) {
                power -= MjAPI.MJ;
            } else if (power > 0) {
                power = 0;
            }
        }

        updateHeatLevel();
        getEnergyStage();
        engineUpdate();

        TileEntity tile = getTileBuffer(currentDirection).getTile();

        if (progressPart != 0) {
            progress += getPistonSpeed();

            if (progress > 0.5 && progressPart == 1) {
                progressPart = 2;
                sendPower(); // Comment out for constant power
            } else if (progress >= 1) {
                progress = 0;
                progressPart = 0;
            }
        } else if (isRedstonePowered && isActive()) {
            if (isPoweredTile(tile, currentDirection)) {
                if (getPowerToExtract() > 0) {
                    progressPart = 1;
                    setPumping(true);
                } else {
                    setPumping(false);
                }
            } else {
                setPumping(false);
            }
        } else {
            setPumping(false);
        }

        // Uncomment for constant power
        // if (isRedstonePowered && isActive()) {
        // sendPower();
        // } else currentOutput = 0;

        burn();
    }

    private long getPowerToExtract() {
        TileEntity tile = getTileBuffer(currentDirection).getTile();

        if (tile == null) return 0;

        if (tile.getClass() == getClass()) {
            TileEngineBase_BC8 other = (TileEngineBase_BC8) tile;
            return other.getMaxPower() - power;
        }

        IMjReceiver receiver = getReceiverToPower(tile, currentDirection);
        if (receiver == null) {
            return 0;
        }

        // Pulsed power
        return extractPower(0, receiver.getPowerRequested(), true);
        // TODO: Use this:
        // return extractPower(receiver.getMinPowerReceived(), receiver.getMaxPowerReceived(), false);

        // Constant power
        // return extractEnergy(0, getActualOutput(), false); // Uncomment for constant power
    }

    private void sendPower() {
        TileEntity tile = getTileBuffer(currentDirection).getTile();
        if (tile == null) {
            return;
        }
        if (getClass() == tile.getClass()) {
            TileEngineBase_BC8 other = (TileEngineBase_BC8) tile;
            if (currentDirection == other.currentDirection) {
                other.power += extractPower(0, power, true);
            }
            return;
        }
        IMjReceiver receiver = getReceiverToPower(tile, currentDirection);
        if (receiver != null) {
            long extracted = getPowerToExtract();
            if (extracted > 0) {
                long excess = receiver.receivePower(extracted, false);
                extractPower(extracted - excess, extracted - excess, true); // Comment out for constant power
                // currentOutput = extractEnergy(0, needed, true); // Uncomment for constant power
            }
        }
    }

    // Uncomment out for constant power
    // public float getActualOutput() {
    // float heatLevel = getIdealHeatLevel();
    // return getCurrentOutput() * heatLevel;
    // }
    protected void burn() {}

    protected void engineUpdate() {
        if (!isRedstonePowered) {
            if (power >= 1) {
                power -= 1;
            } else if (power < 1) {
                power = 0;
            }
        }
    }

    public boolean isActive() {
        return true;
    }

    protected final void setPumping(boolean isActive) {
        if (this.isPumping == isActive) {
            return;
        }

        this.isPumping = isActive;
        sendNetworkUpdate(NET_RENDER_DATA);
    }

    // TEMP
    @FunctionalInterface
    public interface ITileBuffer {
        TileEntity getTile();
    }

    /** Temp! This should be replaced with a tile buffer! */
    public ITileBuffer getTileBuffer(EnumFacing side) {
        TileEntity tile = worldObj.getTileEntity(getPos().offset(side));
        return () -> tile;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        // tileCache = null;
        // checkOrienation = true;
    }

    @Override
    public void validate() {
        super.validate();
        // tileCache = null;
        // checkOrienation = true;
    }

    /* STATE INFORMATION */
    public abstract boolean isBurning();

    // IPowerReceptor stuffs -- move!
    // @Override
    // public PowerReceiver getPowerReceiver(ForgeDirection side) {
    // return powerHandler.getPowerReceiver();
    // }
    //
    // @Override
    // public void doWork(PowerHandler workProvider) {
    // if (worldObj.isRemote) {
    // return;
    // }
    //
    // addEnergy(powerHandler.useEnergy(1, maxEnergyReceived(), true) * 0.95F);
    // }

    public void addPower(long microJoules) {
        power += microJoules;
        lastPower += microJoules;

        if (getEnergyStage() == EnumPowerStage.OVERHEAT) {
            // TODO: turn engine off
            // worldObj.createExplosion(null, xCoord, yCoord, zCoord, explosionRange(), true);
            // worldObj.setBlockToAir(xCoord, yCoord, zCoord);
        }

        if (power > getMaxPower()) {
            power = getMaxPower();
        }
    }

    public long extractPower(long min, long max, boolean doExtract) {
        if (power < min) {
            return 0;
        }

        long actualMax;

        if (max > maxPowerExtracted()) {
            actualMax = maxPowerExtracted();
        } else {
            actualMax = max;
        }

        if (actualMax < min) {
            return 0;
        }

        long extracted;

        if (power >= actualMax) {
            extracted = actualMax;

            if (doExtract) {
                power -= actualMax;
            }
        } else {
            extracted = power;

            if (doExtract) {
                power = 0;
            }
        }

        return extracted;
    }

    public final boolean isPoweredTile(TileEntity tile, EnumFacing side) {
        if (tile == null) return false;
        if (tile.getClass() == getClass()) {
            TileEngineBase_BC8 other = (TileEngineBase_BC8) tile;
            return other.currentDirection == currentDirection;
        }
        return getReceiverToPower(tile, side) != null;
    }

    /** Redstone engines override this to get an {@link IMjRedstoneReceiver} instance */
    public IMjReceiver getReceiverToPower(TileEntity tile, EnumFacing side) {
        if (tile == null) return null;
        IMjReceiver rec = tile.getCapability(MjAPI.CAP_RECEIVER, side.getOpposite());
        if (rec != null && rec.canConnect(mjConnector)) {
            return rec;
        } else {
            return null;
        }
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing == currentDirection) {
            return false;
        }
        return mjCaps.hasCapability(capability, facing) || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing == currentDirection) {
            return null;
        }
        T cap = mjCaps.getCapability(capability, facing);
        if (cap != null) {
            return cap;
        }
        return super.getCapability(capability, facing);
    }

    public abstract long getMaxPower();

    public long minPowerReceived() {
        return 2 * MjAPI.MJ;
    }

    public abstract long maxPowerReceived();

    public abstract long maxPowerExtracted();

    public abstract float explosionRange();

    public long getEnergyStored() {
        return power;
    }

    public abstract long getCurrentOutput();

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("");
        left.add("facing = " + currentDirection);
        left.add("heat = " + heat);
        left.add("power = " + MjAPI.formatMjShort(power));
        left.add("progress = " + progress);
        left.add("last = +" + MjAPI.formatMjShort(lastPower));
    }
}
