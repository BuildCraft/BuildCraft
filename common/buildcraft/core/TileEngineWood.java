/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.enums.EnumEnergyStage;
import buildcraft.api.power.IRedstoneEngine;
import buildcraft.api.power.IRedstoneEngineReceiver;
import buildcraft.api.transport.IPipeTile;
import buildcraft.core.lib.engines.TileEngineBase;

public class TileEngineWood extends TileEngineBase implements IRedstoneEngine {

    private boolean hasSent = false;

    @Override
    public String getResourcePrefix() {
        return "buildcraftcore:textures/blocks/engine/wood";
    }

    @Override
    public ResourceLocation getTrunkTexture(EnumEnergyStage stage) {
        return super.getTrunkTexture(stage == EnumEnergyStage.RED && progress < 0.5 ? EnumEnergyStage.YELLOW : stage);
    }

    @Override
    public int minEnergyReceived() {
        return 0;
    }

    @Override
    public int maxEnergyReceived() {
        return 500;
    }

    @Override
    protected EnumEnergyStage computeEnergyStage() {
        double energyLevel = getEnergyLevel();
        if (energyLevel < 0.33f) {
            return EnumEnergyStage.BLUE;
        } else if (energyLevel < 0.66f) {
            return EnumEnergyStage.GREEN;
        } else if (energyLevel < 0.75f) {
            return EnumEnergyStage.YELLOW;
        } else {
            return EnumEnergyStage.RED;
        }
    }

    @Override
    public float getPistonSpeed() {
        if (!worldObj.isRemote) {
            return Math.max(0.08f * getHeatLevel(), 0.01f);
        }

        switch (getEnergyStage()) {
            case GREEN:
                return 0.02F;
            case YELLOW:
                return 0.04F;
            case RED:
                return 0.08F;
            default:
                return 0.01F;
        }
    }

    @Override
    public void engineUpdate() {
        super.engineUpdate();

        if (isRedstonePowered) {
            if (worldObj.getTotalWorldTime() % 16 == 0) {
                addEnergy(10);
            }
        }
    }

    @Override
    public ConnectOverride overridePipeConnection(IPipeTile.PipeType type, EnumFacing with) {
        return ConnectOverride.DISCONNECT;
    }

    @Override
    public boolean isBurning() {
        return isRedstonePowered;
    }

    @Override
    public int getMaxEnergy() {
        return 1000;
    }

    @Override
    public int calculateCurrentOutput() {
        return 10;
    }

    @Override
    public int maxEnergyExtracted() {
        return 10;
    }

    // TODO: HACK
    @Override
    public boolean canConnectEnergy(EnumFacing from) {
        return false;
    }

    @Override
    protected void sendPower() {
        if (progressPart == 2 && !hasSent) {
            hasSent = true;

            TileEntity tile = getTile(orientation);

            if (tile instanceof IRedstoneEngineReceiver && ((IRedstoneEngineReceiver) tile).canConnectRedstoneEngine(orientation.getOpposite())) {
                super.sendPower();
            } else {
                this.energy = 0;
            }
        } else if (progressPart != 2) {
            hasSent = false;
        }
    }
}
