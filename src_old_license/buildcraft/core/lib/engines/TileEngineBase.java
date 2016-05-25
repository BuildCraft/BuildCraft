/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.engines;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import cofh.api.energy.IEnergyConnection;
import cofh.api.energy.IEnergyHandler;
import cofh.api.energy.IEnergyReceiver;

import buildcraft.BuildCraftCore;
import buildcraft.api.enums.EnumEnergyStage;
import buildcraft.api.enums.EnumEngineType;
import buildcraft.api.power.IEngine;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.tiles.IHeatable;
import buildcraft.api.tools.IToolWrench;
import buildcraft.api.transport.IPipeConnection;
import buildcraft.api.transport.IPipeTile;
import buildcraft.core.CompatHooks;
import buildcraft.core.lib.block.TileBuildCraft;
import buildcraft.core.lib.utils.MathUtils;
import buildcraft.core.lib.utils.Utils;
import buildcraft.core.proxy.CoreProxy;

import io.netty.buffer.ByteBuf;

public abstract class TileEngineBase extends TileBuildCraft implements IPipeConnection, IEnergyHandler, IEngine, IHeatable, IDebuggable {
    // TEMP
    public static final ResourceLocation TRUNK_BLUE_TEXTURE = new ResourceLocation("buildcraftcore:textures/blocks/engine/trunk_blue.png");
    public static final ResourceLocation TRUNK_GREEN_TEXTURE = new ResourceLocation("buildcraftcore:textures/blocks/engine/trunk_green.png");
    public static final ResourceLocation TRUNK_YELLOW_TEXTURE = new ResourceLocation("buildcraftcore:textures/blocks/engine/trunk_yellow.png");
    public static final ResourceLocation TRUNK_RED_TEXTURE = new ResourceLocation("buildcraftcore:textures/blocks/engine/trunk_red.png");
    public static final ResourceLocation TRUNK_OVERHEAT_TEXTURE = new ResourceLocation("buildcraftcore:textures/blocks/engine/trunk_overheat.png");

    public static final float MIN_HEAT = 20;
    public static final float IDEAL_HEAT = 100;
    public static final float MAX_HEAT = 250;
    public int currentOutput = 0;
    public boolean isRedstonePowered = false;
    public float progress;
    public int energy;
    public float heat = MIN_HEAT;
    public EnumEnergyStage energyStage = EnumEnergyStage.BLUE;
    public final EnumEngineType type = getEngineType();
    public EnumFacing orientation = EnumFacing.UP;

    protected int progressPart = 0;

    private boolean checkOrientation = true;
    private boolean checkRedstonePower = true;

    private boolean isPumping = false; // Used for SMP synch
    // How many ticks ago it gave out power, capped to 4.
    private int lastTick = 0;

    public TileEngineBase() {}

    @Override
    public void initialize() {
        checkRedstonePower = true;
    }

    public abstract EnumEngineType getEngineType();

    public boolean onBlockActivated(EntityPlayer player, EnumFacing side) {
        if (!player.worldObj.isRemote && player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem() instanceof IToolWrench) {
            IToolWrench wrench = (IToolWrench) player.getCurrentEquippedItem().getItem();
            if (wrench.canWrench(player, pos)) {
                if (getEnergyStage() == EnumEnergyStage.OVERHEAT && !Utils.isFakePlayer(player)) {
                    energyStage = computeEnergyStage();
                    sendNetworkUpdate();
                }
                checkOrientation = true;

                wrench.wrenchUsed(player, pos);
                return true;
            }
        }
        return false;
    }

    public double getEnergyLevel() {
        return ((double) energy) / getMaxEnergy();
    }

    protected EnumEnergyStage computeEnergyStage() {
        float energyLevel = getHeatLevel();
        if (energyLevel < 0.25f) {
            return EnumEnergyStage.BLUE;
        } else if (energyLevel < 0.5f) {
            return EnumEnergyStage.GREEN;
        } else if (energyLevel < 0.75f) {
            return EnumEnergyStage.YELLOW;
        } else if (energyLevel < 1f) {
            return EnumEnergyStage.RED;
        } else {
            return EnumEnergyStage.OVERHEAT;
        }
    }

    public final EnumEnergyStage getEnergyStage() {
        if (!worldObj.isRemote) {
            if (energyStage == EnumEnergyStage.OVERHEAT) {
                return energyStage;
            }

            EnumEnergyStage newStage = computeEnergyStage();

            if (energyStage != newStage) {
                energyStage = newStage;
                if (energyStage == EnumEnergyStage.OVERHEAT) {
                    overheat();
                }
                sendNetworkUpdate();
            }
        }

        return energyStage;
    }

    public void overheat() {
        this.isPumping = false;
        if (BuildCraftCore.canEnginesExplode) {
            worldObj.createExplosion(null, pos.getX(), pos.getY(), pos.getZ(), 3, true);
            worldObj.setBlockToAir(pos);
        }
    }

    public void updateHeat() {
        heat = (float) ((MAX_HEAT - MIN_HEAT) * getEnergyLevel()) + MIN_HEAT;
    }

    public float getHeatLevel() {
        return (heat - MIN_HEAT) / (MAX_HEAT - MIN_HEAT);
    }

    public float getPistonSpeed() {
        if (!worldObj.isRemote) {
            return Math.max(0.16f * getHeatLevel(), 0.01f);
        }

        switch (getEnergyStage()) {
            case BLUE:
                return 0.02F;
            case GREEN:
                return 0.04F;
            case YELLOW:
                return 0.08F;
            case RED:
                return 0.16F;
            default:
                return 0;
        }
    }

    @Override
    public void update() {
        super.update();

        if (lastTick < 4) {
            lastTick++;
        }

        if (checkRedstonePower) {
            checkRedstonePower();
        }

        if (worldObj.isRemote) {
            if (progressPart != 0) {
                progress += getPistonSpeed();

                if (progress > 1) {
                    progressPart = 0;
                    progress = 0;
                }
            } else if (this.isPumping) {
                progressPart = 1;
            }

            return;
        }

        if (checkOrientation) {
            checkOrientation = false;

            if (!isOrientationValid()) {
                switchOrientation(true);
            }
        }

        updateHeat();
        getEnergyStage();

        if (getEnergyStage() == EnumEnergyStage.OVERHEAT) {
            this.energy = Math.max(this.energy - 50, 0);
            return;
        }

        engineUpdate();

        Object tile = getEnergyProvider(orientation);

        if (progressPart != 0) {
            progress += getPistonSpeed();

            if (progress > 0.5 && progressPart == 1) {
                progressPart = 2;
            } else if (progress >= 1) {
                progress = 0;
                progressPart = 0;
            }
        } else if (isRedstonePowered && isActive()) {
            if (isPoweredTile(tile, orientation)) {
                progressPart = 1;
                setPumping(true);
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

        burn();

        if (!isRedstonePowered) {
            currentOutput = 0;
        } else if (isRedstonePowered && isActive()) {
            sendPower();
        }
    }

    public Object getEnergyProvider(EnumFacing orientation) {
        return CompatHooks.INSTANCE.getEnergyProvider(getTile(orientation));
    }

    private int getPowerToExtract() {
        Object tile = getEnergyProvider(orientation);

        if (tile instanceof IEngine) {
            IEngine engine = (IEngine) tile;

            int maxEnergy = engine.receiveEnergyFromEngine(orientation.getOpposite(), this.energy, true);
            return extractEnergy(maxEnergy, false);
        } else if (tile instanceof IEnergyReceiver) {
            IEnergyReceiver handler = (IEnergyReceiver) tile;

            int maxEnergy = handler.receiveEnergy(orientation.getOpposite(), this.energy, true);
            return extractEnergy(maxEnergy, false);
        } else {
            return 0;
        }
    }

    protected void sendPower() {
        Object tile = getEnergyProvider(orientation);
        if (isPoweredTile(tile, orientation)) {
            int extracted = getPowerToExtract();
            if (extracted <= 0) {
                setPumping(false);
                return;
            }

            setPumping(true);

            if (tile instanceof IEngine) {
                IEngine engine = (IEngine) tile;
                int neededRF = engine.receiveEnergyFromEngine(orientation.getOpposite(), extracted, false);

                extractEnergy(neededRF, true);
            } else if (tile instanceof IEnergyReceiver) {
                IEnergyReceiver handler = (IEnergyReceiver) tile;
                int neededRF = handler.receiveEnergy(orientation.getOpposite(), extracted, false);

                extractEnergy(neededRF, true);
            }
        }
    }

    protected void burn() {}

    protected void engineUpdate() {
        if (!isRedstonePowered) {
            if (energy >= 10) {
                energy -= 10;
            } else if (energy < 10) {
                energy = 0;
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
        lastTick = 0;
        sendNetworkUpdate();
    }

    public boolean isPumping() {
        return isPumping;
    }

    public boolean isOrientationValid() {
        Object tile = getEnergyProvider(orientation);

        return isPoweredTile(tile, orientation);
    }

    public boolean switchOrientation(boolean preferPipe) {
        if (preferPipe && switchOrientationDo(true)) {
            return true;
        } else {
            return switchOrientationDo(false);
        }
    }

    private boolean switchOrientationDo(boolean pipesOnly) {
        for (int i = orientation.getIndex() + 1; i <= orientation.getIndex() + 6; ++i) {
            EnumFacing o = EnumFacing.VALUES[i % 6];

            Object tile = getEnergyProvider(o);

            if ((!pipesOnly || tile instanceof IPipeTile) && isPoweredTile(tile, o)) {
                orientation = o;

                worldObj.markBlockRangeForRenderUpdate(pos, pos);
                worldObj.markBlockForUpdate(pos);
                worldObj.notifyNeighborsRespectDebug(pos, worldObj.getBlockState(pos).getBlock());

                return true;
            }
        }

        return false;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        checkOrientation = true;
    }

    @Override
    public void validate() {
        super.validate();
        checkOrientation = true;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);

        orientation = EnumFacing.values()[data.getByte("orientation")];
        progress = data.getFloat("progress");
        energy = data.getInteger("energy");
        heat = data.getFloat("heat");
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);

        data.setByte("orientation", (byte) orientation.ordinal());
        data.setFloat("progress", progress);
        data.setInteger("energy", energy);
        data.setFloat("heat", heat);
    }

    @Override
    public void readData(ByteBuf stream) {
        int flags = stream.readUnsignedByte();
        energyStage = EnumEnergyStage.values()[flags & 0x07];
        isPumping = (flags & 0x08) != 0;
        orientation = EnumFacing.values()[stream.readByte()];
    }

    @Override
    public void writeData(ByteBuf stream) {
        stream.writeByte(energyStage.ordinal() | (isPumping() ? 8 : 0));
        stream.writeByte(orientation.ordinal());
    }

    public void getGUINetworkData(int id, int value) {
        switch (id) {
            case 0:
                energy = (energy & 0xffff0000) | (value & 0xffff);
                break;
            case 1:
                energy = (energy & 0xffff) | ((value & 0xffff) << 16);
                break;
            case 2:
                currentOutput = value;
                break;
            case 3:
                heat = value / 100F;
                break;
        }
    }

    public void sendGUINetworkData(Container container, ICrafting iCrafting) {
        iCrafting.sendProgressBarUpdate(container, 0, energy & 0xffff);
        iCrafting.sendProgressBarUpdate(container, 1, (energy & 0xffff0000) >> 16);
        iCrafting.sendProgressBarUpdate(container, 2, currentOutput);
        iCrafting.sendProgressBarUpdate(container, 3, Math.round(heat * 100));
    }

    /* STATE INFORMATION */
    public abstract boolean isBurning();

    public void addEnergy(int addition) {
        if (getEnergyStage() == EnumEnergyStage.OVERHEAT) {
            return;
        }

        energy += addition;

        if (energy > getMaxEnergy()) {
            energy = getMaxEnergy();
        }
    }

    public int extractEnergy(int energyMax, boolean doExtract) {
        int max = Math.min(energyMax, getCurrentOutputLimit());

        int extracted;

        if (energy >= max) {
            extracted = max;

            if (doExtract) {
                energy -= max;
            }
        } else {
            extracted = energy;

            if (doExtract) {
                energy = 0;
            }
        }

        return extracted;
    }

    public boolean isPoweredTile(Object tile, EnumFacing side) {
        if (tile == null) {
            return false;
        } else if (tile instanceof IEngine) {
            return ((IEngine) tile).canReceiveFromEngine(side.getOpposite());
        } else if (tile instanceof IEnergyHandler || tile instanceof IEnergyReceiver) {
            return ((IEnergyConnection) tile).canConnectEnergy(side.getOpposite());
        } else {
            return false;
        }
    }

    public abstract int getMaxEnergy();

    public int getEnergyStored() {
        return energy;
    }

    public abstract int getIdealOutput();

    public int getCurrentOutputLimit() {
        return Integer.MAX_VALUE;
    }

    @Override
    public ConnectOverride overridePipeConnection(IPipeTile.PipeType type, EnumFacing with) {
        if (type == IPipeTile.PipeType.POWER) {
            return ConnectOverride.DEFAULT;
        } else if (with == orientation) {
            return ConnectOverride.DISCONNECT;
        } else {
            return ConnectOverride.DEFAULT;
        }
    }

    public void checkRedstonePower() {
        checkRedstonePower = false;
        isRedstonePowered = worldObj.isBlockIndirectlyGettingPowered(pos) > 0;
    }

    public void onNeighborUpdate() {
        checkRedstonePower = true;
        checkOrientation = true;
    }

    // RF support

    @Override
    public int extractEnergy(EnumFacing from, int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored(EnumFacing from) {
        if (!(from == orientation)) {
            return 0;
        }

        return energy;
    }

    @Override
    public int getMaxEnergyStored(EnumFacing from) {
        return this.getMaxEnergy();
    }

    @Override
    public boolean canConnectEnergy(EnumFacing from) {
        return from == orientation;
    }

    // IEngine

    @Override
    public boolean canReceiveFromEngine(EnumFacing side) {
        return side == orientation.getOpposite();
    }

    @Override
    public int receiveEnergyFromEngine(EnumFacing side, int amount, boolean simulate) {
        if (canReceiveFromEngine(side)) {
            int targetEnergy = Math.min(this.getMaxEnergy() - this.energy, amount);
            if (!simulate) {
                energy += targetEnergy;
            }
            return targetEnergy;
        } else {
            return 0;
        }
    }

    // IHeatable

    @Override
    public double getMinHeatValue() {
        return MIN_HEAT;
    }

    @Override
    public double getIdealHeatValue() {
        return IDEAL_HEAT;
    }

    @Override
    public double getMaxHeatValue() {
        return MAX_HEAT;
    }

    @Override
    public double getCurrentHeatValue() {
        return heat;
    }

    @Override
    public double setHeatValue(double value) {
        heat = (float) MathUtils.clamp(value, MIN_HEAT, MAX_HEAT);
        return heat;
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        TileEngineBase server = CoreProxy.proxy.getServerTile(this);
        left.add("");
        left.add("orient = " + server.orientation);
        left.add("isRemote = " + server.worldObj.isRemote);
    }
}
