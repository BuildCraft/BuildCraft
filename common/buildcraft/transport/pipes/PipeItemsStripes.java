/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.pipes;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.WorldServer;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.core.Position;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.transport.IStripesHandler;
import buildcraft.api.transport.IStripesHandler.StripesAction;
import buildcraft.api.transport.IStripesHandler.StripesBehavior;
import buildcraft.api.transport.IStripesPipe;
import buildcraft.api.transport.PipeManager;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.TravelingItem;
import buildcraft.transport.pipes.events.PipeEventItem;
import buildcraft.transport.stripes.DefaultHandler;
import buildcraft.transport.utils.TransportUtils;


public class PipeItemsStripes extends Pipe<PipeTransportItems> implements IStripesPipe {

	// A lot excluded due to right-click functionality, already doing the thing
	public static final Item[] DISPENCER_EXCLUSIONS = {
		Item.getItemFromBlock(Blocks.tnt), Items.dye/*Bonemeal*/,
		Items.boat, Items.minecart, Items.chest_minecart, Items.hopper_minecart,
		Items.tnt_minecart, Items.furnace_minecart, Items.command_block_minecart
	};

	@MjBattery(maxCapacity = 1, maxReceivedPerCycle = 1, minimumConsumption = 0)
	private double mjStored = 0;

	public PipeItemsStripes(Item pipeItem) {
		super(new PipeTransportItems(), pipeItem);
	}

	@Override
	public IIconProvider getIconProvider() {
		return BuildCraftTransport.instance.pipeIconProvider;
	}

	@Override
	public int getIconIndex(ForgeDirection direction) {
		return PipeIconProvider.TYPE.Stripes.ordinal();
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		if (!getWorld().isRemote && mjStored > 0) {
			ForgeDirection dir = getOpenOrientation();
			if (dir != ForgeDirection.UNKNOWN) {
				Position loc = getPosition().shift(dir);
				Block block = loc.getBlock(getWorld());
				float h = block.getBlockHardness(getWorld(), (int) loc.x, (int) loc.y, (int) loc.z);
				if (loc.blockExists(getWorld()) && h > 0.0F && h < 100.0F) {
					IStripesHandler handler = new DefaultHandler();
					Item blockItem = Item.getItemFromBlock(block);
					if (PipeManager.stripesHandlers.containsKey(blockItem)) {
						handler = PipeManager.stripesHandlers.get(blockItem);
					} else if (blockItem instanceof IStripesHandler) {
						handler = (IStripesHandler) blockItem;
					} else if (block instanceof IStripesHandler) {
						handler = (IStripesHandler) block;
					}
					if (handler.behave(this, StripesAction.DESTROY, new ItemStack(block, 0, loc.getMeta(getWorld()))) ==
							StripesBehavior.DEFAULT) {
						List<ItemStack> drops = block.getDrops(getWorld(), (int) loc.x, (int) loc.y, (int) loc.z, loc.getMeta(getWorld()), 0);
						loc.destroyBlock(getWorld(), false);
						for (ItemStack drop : drops) {
							if (drop != null) {
								sendItem(drop, dir.getOpposite());
							}
						}
					}
				}
			}
			mjStored = 0;
		}
	}

	public void eventHandler(PipeEventItem.DropItem e) {
		ItemStack is = e.item.getItemStack();
		ForgeDirection dir = e.direction;
		Position pipe = getPosition();
		Position loc = pipe.copy().shift(dir);
		ForgeDirection temp = ForgeDirection.DOWN;
		if (dir == ForgeDirection.UP) {
			temp = ForgeDirection.UP;
		}
		IStripesHandler handler = new DefaultHandler();
		if (PipeManager.stripesHandlers.containsKey(is.getItem())) {
			handler = PipeManager.stripesHandlers.get(is.getItem());
		} else if (is.getItem() instanceof IStripesHandler) {
			handler = (IStripesHandler) is.getItem();
		} else if (is.getItem() instanceof ItemBlock) {
			Block block = Block.getBlockFromItem(is.getItem());
			if (block instanceof IStripesHandler) {
				handler = (IStripesHandler) block;
			}
		}
		StripesBehavior behavior = handler.behave(this, StripesAction.PLACE, is);
		switch (behavior) {
			case DEFAULT:
				if (!loc.blockExists(getWorld())) {
					if (loc.shift(temp).blockExists(getWorld()) && activate(is, loc, pipe, temp.getOpposite(), dir)) {
						break;
					}
					if (loc.shift(temp.getOpposite()).shift(dir).blockExists(getWorld()) && activate(is, loc, pipe, dir.getOpposite(), dir)) {
						break;
					}
					activate(is, loc.shift(dir.getOpposite(), 2), pipe, dir, dir);
				} else {
					activate(is, loc, pipe, dir.getOpposite(), dir);
				}
				break;
			case NONE:
				is.stackSize = 0;
		}
	}

	private boolean activate(ItemStack is, Position loc, Position player, ForgeDirection clickOn, ForgeDirection pipe) {
		if (getWorld().isRemote) {
			return false;
		}
		int offset = 0;
		switch (pipe) {
			case DOWN:
				offset = -1;
				break;
			case UP:
				offset = 1;
		}
		EntityPlayer fake = CoreProxy.proxy.getBuildCraftPlayer((WorldServer) getWorld(), (int) player.x, (int) player.y + offset, (int) player.z).get();
		fake.rotationYaw = new float[]{0.0F, 0.0F, -180.0F, 0.0F, 90.0F, -90.0F}[pipe.ordinal()];
		fake.rotationPitch = offset * 90.0F;
		fake.inventory.currentItem = 0;
		fake.inventory.setInventorySlotContents(0, is);
		return loc.getBlock(getWorld()).onBlockActivated(getWorld(), (int) loc.x, (int) loc.y, (int) loc.z, fake, clickOn.ordinal(), 0.5F, 0.5F, 0.5F)
				|| is.tryPlaceItemIntoWorld(fake, getWorld(), (int) loc.x, (int) loc.y, (int) loc.z, clickOn.ordinal(), 0.5F, 0.5F, 0.5F);
	}

	@Override
	public boolean canPipeConnect(TileEntity tile, ForgeDirection side) {
		if (tile instanceof TileGenericPipe) {
			Pipe pipe = ((TileGenericPipe) tile).pipe;
			return !(pipe instanceof PipeItemsStripes) && pipe.transport instanceof PipeTransportItems;
		}
		return false;
	}

	@Override
	public Position getPosition() {
		return new Position(getContainer());
	}

	@Override
	public void sendItem(ItemStack itemStack, ForgeDirection direction) {
		TravelingItem newItem = TravelingItem.make(
			container.xCoord + 0.5,
			container.yCoord + TransportUtils.getPipeFloorOf(itemStack),
			container.zCoord + 0.5, itemStack);
		transport.injectItem(newItem, direction);
	}
}
