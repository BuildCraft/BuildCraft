/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.pipes;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.core.GuiIds;
import buildcraft.core.inventory.SimpleInventory;
import buildcraft.core.network.IClientState;
import buildcraft.core.utils.Utils;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.IDiamondPipe;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportFluids;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

public class PipeFluidsDiamond extends Pipe<PipeTransportFluids> implements IDiamondPipe {

    private SimpleInventory filters = new SimpleInventory(54, "Filters", 1);

	public PipeFluidsDiamond(Item item) {
	    super(new PipeTransportFluids(), item);

        transport.initFromPipe(getClass());
		transport.travelDelay = 4;
	}

    public IInventory getFilters() {
        return filters;
    }

	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return BuildCraftTransport.instance.pipeIconProvider;
	}

	@Override
    public int getIconIndex(ForgeDirection direction) {
        switch (direction) {
            case UNKNOWN:
                return PipeIconProvider.TYPE.PipeFluidsDiamond_Center.ordinal();
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
                throw new IllegalArgumentException("direction out of bounds");
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

        if (!container.getWorldObj().isRemote) {
            entityplayer.openGui(BuildCraftTransport.instance, GuiIds.PIPE_DIAMOND, container.getWorldObj(), container.xCoord, container.yCoord, container.zCoord);
        }

        return true;
    }

    /* SAVING & LOADING */
    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        filters.readFromNBT(nbt);
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
