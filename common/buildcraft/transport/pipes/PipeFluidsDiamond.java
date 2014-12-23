/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.pipes;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidTankInfo;
import buildcraft.BuildCraftTransport;
import buildcraft.core.GuiIds;
import buildcraft.core.inventory.SimpleInventory;
import buildcraft.core.utils.FluidUtils;
import buildcraft.core.utils.Utils;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.IDiamondPipe;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportFluids;

public class PipeFluidsDiamond extends Pipe<PipeTransportFluids> implements IDiamondPipe {

	private class FilterInventory extends SimpleInventory {
		public boolean[] filteredDirections = new boolean[6];
		public Fluid[] fluids = new Fluid[54];

		public FilterInventory(int size, String invName, int invStackLimit) {
			super(size, invName, invStackLimit);
		}

		@Override
		public boolean isItemValidForSlot(int slot, ItemStack stack) {
			return stack == null || FluidUtils.isFluidContainer(stack);
		}

		@Override
		public void markDirty() {
			// calculate fluid cache
			for (int i = 0; i < 6; i++) {
				filteredDirections[i] = false;
			}

			for (int i = 0; i < 54; i++) {
				fluids[i] = FluidUtils.getFluidFromItemStack(getStackInSlot(i));
				if (fluids[i] != null) {
					filteredDirections[i / 9] = true;
				}
			}
		}
	}

    private FilterInventory filters = new FilterInventory(54, "Filters", 1);

	public PipeFluidsDiamond(Item item) {
	    super(new PipeTransportFluids(), item);

        transport.initFromPipe(getClass());
	}

    @Override
    public IInventory getFilters() {
        return filters;
    }

	/*@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return BuildCraftTransport.instance.pipeIconProvider;
	}*/

	@Override
    public int getIconIndex(EnumFacing direction) {
        switch (direction) {
            case DOWN:
                return PipeIconProvider.TYPE.PipeFluidsDiamond_Down.ordinal();
            case UP:
                return PipeIconProvider.TYPE.PipeFluidsDiamond_Up.ordinal();
            case NORTH:
                return PipeIconProvider.TYPE.PipeFluidsDiamond_North.ordinal();
            case SOUTH:
                return PipeIconProvider.TYPE.PipeFluidsDiamond_South.ordinal();
            case WEST:
                return PipeIconProvider.TYPE.PipeFluidsDiamond_West.ordinal();
            case EAST:
                return PipeIconProvider.TYPE.PipeFluidsDiamond_East.ordinal();
            default:
            	return PipeIconProvider.TYPE.PipeFluidsDiamond_Center.ordinal();
        }
    }

    @Override
    public int getIconIndexForItem() {
        return PipeIconProvider.TYPE.PipeFluidsDiamond_Item.ordinal();
    }

    @Override
    public boolean blockActivated(EntityPlayer entityplayer) {
        if (entityplayer.getCurrentEquippedItem() != null) {
            if (Block.getBlockFromItem(entityplayer.getCurrentEquippedItem().getItem()) instanceof BlockGenericPipe) {
                return false;
            }
        }

        if (!container.getWorld().isRemote) {
            entityplayer.openGui(BuildCraftTransport.instance, GuiIds.PIPE_DIAMOND, container.getWorld(), container.getPos().getX(), container.getPos().getY(), container.getPos().getZ());
        }

        return true;
    }

	@Override
	public boolean outputOpen(EnumFacing to) {
		if (!super.outputOpen(to)) {
			return false;
		}

		// get center tank, from which outputs are checked; ignore if has no fluid
		FluidTankInfo[] tanks = transport.getTankInfo(null);
		if (tanks == null || tanks[0] == null || tanks[0].fluid == null || tanks[0].fluid.amount == 0) {
			return true;
		}

		Fluid fluidInTank = tanks[0].fluid.getFluid();
		boolean[] validFilter = new boolean[6];
		boolean isFiltered = false;
		for (EnumFacing dir : EnumFacing.values()) {
			if (container.isPipeConnected(dir) && filters.filteredDirections[dir.ordinal()]) {
				for (int slot = dir.ordinal() * 9; slot < dir.ordinal() * 9 + 9; ++slot) {
					if (filters.fluids[slot] != null && filters.fluids[slot].getID() == fluidInTank.getID()) {
						validFilter[dir.ordinal()] = true;
						isFiltered = true;
						break;
					}
				}
			}
		}
		// the direction is filtered and liquids match
		if (filters.filteredDirections[to.ordinal()] && validFilter[to.ordinal()]) {
			return true;
		}

		// we haven't found a filter for this liquid and the direction is free
		if (!isFiltered && !filters.filteredDirections[to.ordinal()]) {
			return true;
		}

		// we have a filter for the liquid, but not a valid direction
		return false;
	}

    /* SAVING & LOADING */
    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        filters.readFromNBT(nbt);
	    filters.markDirty();
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        filters.writeToNBT(nbt);
    }

    // ICLIENTSTATE
    @Override
    public void writeData(ByteBuf data) {
        NBTTagCompound nbt = new NBTTagCompound();
        writeToNBT(nbt);
        Utils.writeNBT(data, nbt);
    }

    @Override
    public void readData(ByteBuf data) {
        NBTTagCompound nbt = Utils.readNBT(data);
        readFromNBT(nbt);
    }
}
