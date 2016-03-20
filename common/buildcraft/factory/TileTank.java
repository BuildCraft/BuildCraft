/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.factory;

import java.util.List;
import io.netty.buffer.ByteBuf;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.EnumSkyBlock;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import buildcraft.BuildCraftCore;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.core.lib.block.TileBuildCraft;
import buildcraft.core.lib.fluids.Tank;
import buildcraft.core.lib.fluids.TankManager;
import buildcraft.core.lib.utils.BlockUtils;

public class TileTank extends TileBuildCraft implements IFluidHandler, IDebuggable {
    public final Tank tank = new Tank("tank", FluidContainerRegistry.BUCKET_VOLUME * 16, this);
    public final TankManager<Tank> tankManager = new TankManager<>(tank);
    public boolean hasUpdate = false;
    public boolean hasNetworkUpdate = false;
    public SafeTimeTracker tracker = new SafeTimeTracker(2 * BuildCraftCore.updateFactor);
    private int prevLightValue = 0;
    private int cachedComparatorOverride = 0;

    @Override
    public void initialize() {
        super.initialize();
        updateComparators();
    }

    public void updateComparators() {
        int co = calculateComparatorInputOverride();
        TileTank uTank = getBottomTank();
        while (uTank != null) {
            uTank.cachedComparatorOverride = co;
            uTank.hasUpdate = true;
            uTank = getTankAbove(uTank);
        }
    }

    public void onBlockBreak() {
        if (!tank.isEmpty()) {
            FluidEvent.fireEvent(new FluidEvent.FluidSpilledEvent(tank.getFluid(), worldObj, pos));
        }
    }

    /* UPDATING */
    @Override
    public void update() {
        super.update();

        if (init != 2 || worldObj == null) return;

        if (worldObj.isRemote) {
            int lightValue = getFluidLightLevel();
            if (prevLightValue != lightValue) {
                prevLightValue = lightValue;
                worldObj.setLightFor(EnumSkyBlock.BLOCK, pos, lightValue);
            }
            return;
        }

        // Have liquid flow down into tanks below if any.
        if (tank.getFluid() != null) {
            moveFluidBelow();
        }

        if (hasUpdate) {
            worldObj.notifyBlockOfStateChange(pos, blockType);
            BlockUtils.onComparatorUpdate(worldObj, pos, getBlockType());
            hasUpdate = false;
        }

        if (hasNetworkUpdate && tracker.markTimeIfDelay(worldObj)) {
            sendNetworkUpdate();
            hasNetworkUpdate = false;
        }
    }

    /* NETWORK */
    @Override
    public void writeData(ByteBuf data) {
        tankManager.writeData(data);
    }

    @Override
    public void readData(ByteBuf stream) {
        tankManager.readData(stream);
    }

    /* SAVING & LOADING */
    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        tankManager.readFromNBT(data);
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        tankManager.writeToNBT(data);
    }

    /* HELPER FUNCTIONS */

    /** @return Last tank block below this one or this one if it is the last. */
    public TileTank getBottomTank() {

        TileTank lastTank = this;

        while (true) {
            TileTank below = getTankBelow(lastTank);
            if (below != null) {
                lastTank = below;
            } else {
                break;
            }
        }

        return lastTank;
    }

    public TileTank getTopTank() {
        TileTank lastTank = this;

        while (true) {
            TileTank above = getTankAbove(lastTank);
            if (above != null) {
                lastTank = above;
            } else {
                break;
            }
        }

        return lastTank;
    }

    public static TileTank getTankBelow(TileTank tile) {
        TileEntity below = tile.getTile(EnumFacing.DOWN);
        if (below instanceof TileTank) {
            if (!below.hasWorldObj()) return null;
            return (TileTank) below;
        } else {
            return null;
        }
    }

    public static TileTank getTankAbove(TileTank tile) {
        TileEntity above = tile.getTile(EnumFacing.UP);
        if (above instanceof TileTank) {
            if (!above.hasWorldObj()) return null;
            return (TileTank) above;
        } else {
            return null;
        }
    }

    public void moveFluidBelow() {
        TileTank below = getTankBelow(this);
        if (below == null) {
            return;
        }

        int oldComparator = getComparatorInputOverride();
        int used = below.tank.fill(tank.getFluid(), true);

        if (used > 0) {
            hasNetworkUpdate = true; // not redundant because tank.drain operates on an IFluidTank, not a tile
            below.hasNetworkUpdate = true; // redundant because below.fill sets hasUpdate

            if (oldComparator != calculateComparatorInputOverride()) {
                updateComparators();
            }

            tank.drain(used, true);
        }
    }

    /* ITANKCONTAINER */
    @Override
    public int fill(EnumFacing from, FluidStack resource, boolean doFill) {
        if (resource == null) {
            return 0;
        }

        FluidStack resourceCopy = resource.copy();
        int totalUsed = 0;
        TileTank tankToFill = getBottomTank();

        FluidStack liquid = tankToFill.tank.getFluid();
        if (liquid != null && liquid.amount > 0 && !liquid.isFluidEqual(resourceCopy)) {
            return 0;
        }

        int oldComparator = getComparatorInputOverride();

        while (tankToFill != null && resourceCopy.amount > 0) {
            int used = tankToFill.tank.fill(resourceCopy, doFill);
            resourceCopy.amount -= used;
            if (used > 0) {
                tankToFill.hasNetworkUpdate = true;
            }

            totalUsed += used;
            tankToFill = getTankAbove(tankToFill);
        }

        if (oldComparator != calculateComparatorInputOverride()) {
            updateComparators();
        }

        return totalUsed;
    }

    @Override
    public FluidStack drain(EnumFacing from, int maxEmpty, boolean doDrain) {
        TileTank bottom = getBottomTank();
        bottom.hasNetworkUpdate = true;
        int oldComparator = getComparatorInputOverride();
        FluidStack output = bottom.tank.drain(maxEmpty, doDrain);

        if (oldComparator != calculateComparatorInputOverride()) {
            updateComparators();
        }

        return output;
    }

    @Override
    public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain) {
        if (resource == null) {
            return null;
        }
        TileTank bottom = getBottomTank();
        if (!resource.isFluidEqual(bottom.tank.getFluid())) {
            return null;
        }
        return drain(from, resource.amount, doDrain);
    }

    @Override
    public FluidTankInfo[] getTankInfo(EnumFacing direction) {
        FluidTank compositeTank = new FluidTank(tank.getCapacity());

        TileTank tile = getBottomTank();

        if (tile != null && tile.tank.getFluid() != null) {
            compositeTank.setFluid(tile.tank.getFluid().copy());
        } else {
            return new FluidTankInfo[] { compositeTank.getInfo() };
        }

        int capacity = tile.tank.getCapacity();
        tile = getTankAbove(tile);

        while (tile != null) {
            FluidStack liquid = tile.tank.getFluid();
            if (liquid == null || liquid.amount == 0) {
                // NOOP
            } else if (!compositeTank.getFluid().isFluidEqual(liquid)) {
                break;
            } else {
                compositeTank.getFluid().amount += liquid.amount;
            }

            capacity += tile.tank.getCapacity();
            tile = getTankAbove(tile);
        }

        compositeTank.setCapacity(capacity);
        return new FluidTankInfo[] { compositeTank.getInfo() };
    }

    @Override
    public boolean canFill(EnumFacing from, Fluid fluid) {
        Fluid tankFluid = getBottomTank().tank.getFluidType();
        return tankFluid == null || tankFluid == fluid;
    }

    @Override
    public boolean canDrain(EnumFacing from, Fluid fluid) {
        Fluid tankFluid = getBottomTank().tank.getFluidType();
        return tankFluid != null && tankFluid == fluid;
    }

    public int getFluidLightLevel() {
        FluidStack tankFluid = tank.getFluid();
        return tankFluid == null || tankFluid.amount == 0 ? 0 : tankFluid.getFluid().getLuminosity(tankFluid);
    }

    public int calculateComparatorInputOverride() {
        FluidTankInfo[] info = getTankInfo(null);
        if (info.length > 0 && info[0] != null && info[0].fluid != null) {
            return info[0].fluid.amount * 15 / info[0].capacity;
        } else {
            return 0;
        }
    }

    public int getComparatorInputOverride() {
        return cachedComparatorOverride;
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("");
        left.add(tank.getFluidAmount() + "/" + tank.getCapacity() + "mB");
        left.add(tank.getFluid() == null ? "empty" : tank.getFluidType().getLocalizedName(tank.getFluid()));
    }
}
