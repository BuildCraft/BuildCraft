/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.engine;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.world.biome.Biome;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.enums.EnumPowerStage;
import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.IMjRedstoneReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjCapabilityHelper;
import buildcraft.api.tiles.IDebuggable;

import buildcraft.lib.block.VanillaRotationHandlers;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.collect.OrderedEnumMap;
import buildcraft.lib.misc.data.ModelVariableData;
import buildcraft.lib.net.PacketBufferBC;
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

    protected double heat = MIN_HEAT;// TODO: sync gui data
    protected long power = 0;// TODO: sync gui data
    private long lastPower = 0;
    /** Increments from 0 to 1. Above 0.5 all of the held power is emitted. */
    private float progress, lastProgress;
    private int progressPart = 0;

    protected EnumPowerStage powerStage = EnumPowerStage.BLUE;
    protected EnumFacing currentDirection = EnumFacing.UP;

    public long currentOutput;// TODO: sync gui data
    public boolean isRedstonePowered = false;
    protected boolean isPumping = false;

    /** The model variables, used to keep track of the various state-based variables. */
    public final ModelVariableData clientModelData = new ModelVariableData();

    // Needed: Power stored

    public TileEngineBase_BC8() {}

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        currentDirection = NBTUtilBC.readEnum(nbt.getTag("currentDirection"), EnumFacing.class);
        if (currentDirection == null) {
            currentDirection = EnumFacing.UP;
        }
        heat = nbt.getDouble("heat");
        power = nbt.getLong("power");
        progress = nbt.getFloat("progress");
        progressPart = nbt.getInteger("progressPart");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setTag("currentDirection", NBTUtilBC.writeEnum(currentDirection));
        nbt.setDouble("heat", heat);
        nbt.setLong("power", power);
        nbt.setFloat("progress", progress);
        nbt.setInteger("progressPart", progressPart);
        return nbt;
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == Side.CLIENT) {
            if (id == NET_RENDER_DATA) {
                isPumping = buffer.readBoolean();
                currentDirection = buffer.readEnumValue(EnumFacing.class);
                powerStage = buffer.readEnumValue(EnumPowerStage.class);
                progress = buffer.readFloat();
            } else if (id == NET_GUI_DATA) {
                heat = buffer.readFloat();
                currentOutput = buffer.readLong();
                power = buffer.readLong();
            } else if (id == NET_GUI_TICK) {
                heat = buffer.readFloat();
                currentOutput = buffer.readLong();
                power = buffer.readLong();

            }
        }
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Side side) {
        super.writePayload(id, buffer, side);
        if (side == Side.SERVER) {
            if (id == NET_RENDER_DATA) {
                buffer.writeBoolean(isPumping);
                buffer.writeEnumValue(currentDirection);
                buffer.writeEnumValue(powerStage);
                buffer.writeFloat(progress);
            } else if (id == NET_GUI_DATA) {
                buffer.writeFloat((float) heat);
                buffer.writeLong(currentOutput);
                buffer.writeLong(power);
            } else if (id == NET_GUI_TICK) {
                buffer.writeFloat((float) heat);
                buffer.writeLong(currentOutput);
                buffer.writeLong(power);

            }
        }
    }

    public EnumActionResult attemptRotation() {
        OrderedEnumMap<EnumFacing> possible = VanillaRotationHandlers.ROTATE_FACING;
        EnumFacing current = currentDirection;
        for (int i = 0; i < 6; i++) {
            current = possible.next(current);
            if (isFacingReceiver(current)) {
                if (currentDirection != current) {
                    currentDirection = current;
                    // makeTileCache();
                    sendNetworkUpdate(NET_RENDER_DATA);
                    redrawBlock();
                    world.notifyNeighborsRespectDebug(getPos(), getBlockType(), true);
                    return EnumActionResult.SUCCESS;
                }
                return EnumActionResult.FAIL;
            }
        }
        return EnumActionResult.FAIL;
    }

    private boolean isFacingReceiver(EnumFacing dir) {
        TileEntity neighbour = world.getTileEntity(getPos().offset(dir));
        if (neighbour == null) return false;
        IMjConnector other = neighbour.getCapability(MjAPI.CAP_CONNECTOR, dir.getOpposite());
        if (other == null) return false;
        return mjConnector.canConnect(other) && other.canConnect(mjConnector);
    }

    public void rotateIfInvalid() {
        if (currentDirection != null && isFacingReceiver(currentDirection)) {
            return;
        }
        attemptRotation();
        if (currentDirection == null) {
            currentDirection = EnumFacing.UP;
        }
    }

    @Override
    public void onPlacedBy(EntityLivingBase placer, ItemStack stack) {
        super.onPlacedBy(placer, stack);
        currentDirection = null;// Force rotateIfInvalid to always attempt to rotate
        rotateIfInvalid();
    }

    public boolean onActivated(EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        return false;
    }

    protected Biome getBiome() {
        // TODO: Cache this!
        return world.getBiome(getPos());
    }

    /** @return The heat of the current biome, in celsius. */
    protected float getBiomeHeat() {
        Biome biome = getBiome();
        float temp = biome.getFloatTemperature(getPos());
        return Math.max(0, Math.min(30, temp * 15f));
    }

    public double getPowerLevel() {
        return power / (double) getMaxPower();
    }

    protected EnumPowerStage computePowerStage() {
        double heatLevel = getHeatLevel();
        if (heatLevel < 0.25f) return EnumPowerStage.BLUE;
        else if (heatLevel < 0.5f) return EnumPowerStage.GREEN;
        else if (heatLevel < 0.75f) return EnumPowerStage.YELLOW;
        else if (heatLevel < 0.85f) return EnumPowerStage.RED;
        else return EnumPowerStage.OVERHEAT;
    }

    public final EnumPowerStage getPowerStage() {
        if (!world.isRemote) {
            EnumPowerStage newStage = computePowerStage();

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
        if (!world.isRemote) {
            return Math.max(0.16 * getHeatLevel(), 0.01);
        }

        switch (getPowerStage()) {
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

        boolean overheat = getPowerStage() == EnumPowerStage.OVERHEAT;

        if (world.isRemote) {
            lastProgress = progress;

            if (isPumping) {
                progress += getPistonSpeed();

                if (progress >= 1) {
                    progress = 0;
                }
            } else if (progress > 0) {
                progress -= 0.01f;
            }
            clientModelData.tick();
            return;
        }

        lastPower = 0;
        isRedstonePowered = world.isBlockIndirectlyGettingPowered(getPos()) > 0;

        if (!isRedstonePowered) {
            if (power > MjAPI.MJ) {
                power -= MjAPI.MJ;
            } else if (power > 0) {
                power = 0;
            }
        }

        updateHeatLevel();
        getPowerStage();
        engineUpdate();

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
            if (getPowerToExtract(false) > 0) {
                progressPart = 1;
                setPumping(true);
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

        if (!overheat) {
            burn();
        }
    }

    private long getPowerToExtract(boolean doExtract) {
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
        return extractPower(0, receiver.getPowerRequested(), doExtract);
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
        if (receiver != null && mjConnector.canConnect(receiver) && receiver.canConnect(mjConnector)) {
            long extracted = getPowerToExtract(true);
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
        TileEntity tile = world.getTileEntity(getPos().offset(side));
        return () -> tile;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        // tileCache = null;
        // checkOrientation = true;
    }

    @Override
    public void validate() {
        super.validate();
        // tileCache = null;
        // checkOrientation = true;
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

        if (getPowerStage() == EnumPowerStage.OVERHEAT) {
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
        if (rec != null && rec.canConnect(mjConnector) && mjConnector.canConnect(rec)) {
            return rec;
        } else {
            return null;
        }
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
        if (facing == currentDirection) {
            return mjCaps.getCapability(capability, facing);
        } else {
            return super.getCapability(capability, facing);
        }
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

    public boolean isEngineOn() {
        return isPumping;
    }

    @SideOnly(Side.CLIENT)
    public float getProgressClient(float partialTicks) {
        float last = lastProgress;
        float now = progress;
        if (last > 0.5 && now < 0.5) {
            // we just returned
            now += 1;
        }
        float interp = last * (1 - partialTicks) + now * partialTicks;
        return interp % 1;
    }

    public EnumFacing getCurrentFacing() {
        return currentDirection;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("");
        left.add("facing = " + currentDirection);
        left.add("heat = " + LocaleUtil.localizeHeat(heat) + " -- " + String.format("%.2f %%", getHeatLevel()));
        left.add("power = " + LocaleUtil.localizeMj(power));
        left.add("stage = " + powerStage);
        left.add("progress = " + progress);
        left.add("last = " + LocaleUtil.localizeMjFlow(lastPower));
        if (world.isRemote) {
            left.add("Current Model Variables:");
            clientModelData.addDebugInfo(left);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasFastRenderer() {
        return true;
    }
}
