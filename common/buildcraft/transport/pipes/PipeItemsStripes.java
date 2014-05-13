/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.pipes;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLeavesBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.WorldServer;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.core.Position;
import buildcraft.api.mj.MjBattery;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.BlockUtil;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.ItemPipe;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.TravelingItem;
import buildcraft.transport.pipes.events.PipeEventItem;
import buildcraft.transport.utils.TransportUtils;

public class PipeItemsStripes extends Pipe<PipeTransportItems> {

	@MjBattery(maxCapacity = 1, maxReceivedPerCycle = 1, minimumConsumption = 0)
	private double mjStored = 0;

	public PipeItemsStripes(Item item) {
		super(new PipeTransportItems(), item);
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (container.getWorldObj().isRemote) {
			return;
		}

		if (mjStored > 0) {
			ForgeDirection o = getOpenOrientation();

			if (o != ForgeDirection.UNKNOWN) {
				Position p = new Position(container.xCoord, container.yCoord,
						container.zCoord, o);
				p.moveForwards(1.0);

				if (!BlockUtil.isUnbreakableBlock(getWorld(), (int) p.x, (int) p.y, (int) p.z)) {
					ArrayList<ItemStack> stacks = getWorld().getBlock(
							(int) p.x, (int) p.y, (int) p.z).getDrops(
							getWorld(),
							(int) p.x,
							(int) p.y,
							(int) p.z,
							getWorld().getBlockMetadata((int) p.x, (int) p.y,
									(int) p.z), 0
					);

					if (stacks != null) {
						for (ItemStack s : stacks) {
							if (s != null) {
								rollbackItem(s, o);
							}
						}
					}

					getWorld().setBlockToAir((int) p.x, (int) p.y, (int) p.z);
				}
			}
		}

		mjStored = 0;
	}

	public void eventHandler(PipeEventItem.DropItem event) {
		Position p = new Position(container.xCoord, container.yCoord,
				container.zCoord, event.direction);
		Position from = new Position(p);
		p.moveForwards(1.0);

		ItemStack stack = event.entity.getEntityItem();

		if (convertPipe(transport, event.item)) {
			BuildCraftTransport.pipeItemsStripes.onItemUse(new ItemStack(
							BuildCraftTransport.pipeItemsStripes), CoreProxy
					.proxy.getBuildCraftPlayer((WorldServer) getWorld()).get(), getWorld(), (int) p.x,
					(int) p.y, (int) p.z, 1, 0, 0, 0
			);
		} else if (stack.getItem() instanceof ItemBlock) {
			if (getWorld().getBlock((int) p.x, (int) p.y, (int) p.z) == Blocks.air) {
				stack.tryPlaceItemIntoWorld(
						CoreProxy.proxy.getBuildCraftPlayer((WorldServer) getWorld()).get(),
					getWorld(), (int) p.x, (int) p.y, (int) p.z, 1, 0.0f, 0.0f,
					0.0f);
			}
		} else if (stack.getItem() == Items.shears) {
			Block block = getWorld().getBlock((int) p.x, (int) p.y, (int) p.z);

			if (block instanceof BlockLeavesBase) {
				getWorld().playSoundEffect((int) p.x, (int) p.y, (int) p.z, Block.soundTypeGrass.getBreakSound(), 1, 1);
				getWorld().setBlockToAir((int) p.x, (int) p.y, (int) p.z);
				stack.damageItem(1, CoreProxy.proxy.getBuildCraftPlayer((WorldServer) getWorld()).get());
			}
		} else if (stack.getItem() == Items.arrow) {
			stack.stackSize--;

			ForgeDirection direction = event.direction;
			EntityArrow entityArrow = new EntityArrow(getWorld(),
					CoreProxy.proxy.getBuildCraftPlayer((WorldServer) getWorld()).get(), 0);
			entityArrow.setPosition(p.x + 0.5d, p.y + 0.5d, p.z + 0.5d);
			entityArrow.setDamage(3);
			entityArrow.setKnockbackStrength(1);
			entityArrow.motionX = direction.offsetX * 1.8d + getWorld().rand.nextGaussian() * 0.007499999832361937D;
			entityArrow.motionY = direction.offsetY * 1.8d + getWorld().rand.nextGaussian() * 0.007499999832361937D;
			entityArrow.motionZ = direction.offsetZ * 1.8d + getWorld().rand.nextGaussian() * 0.007499999832361937D;
			getWorld().spawnEntityInWorld(entityArrow);
		} else if ((stack.getItem() == Items.potionitem && ItemPotion.isSplash(stack.getItemDamage()))
				   || stack.getItem() == Items.egg
				   || stack.getItem() == Items.snowball) {
			EntityPlayer player = CoreProxy.proxy.getBuildCraftPlayer((WorldServer) getWorld(),
					(int) p.x, (int) p.y, (int) p.z).get();

			switch (event.direction) {
			case DOWN:
				player.rotationPitch = 90;
				player.rotationYaw = 0;
				break;
			case UP:
				player.rotationPitch = 270;
				player.rotationYaw = 0;
				break;
			case NORTH:
				player.rotationPitch = 0;
				player.rotationYaw = 180;
				break;
			case SOUTH:
				player.rotationPitch = 0;
				player.rotationYaw = 0;
				break;
			case WEST:
				player.rotationPitch = 0;
				player.rotationYaw = 90;
				break;
			case EAST:
				player.rotationPitch = 0;
				player.rotationYaw = 270;
				break;
			case UNKNOWN:
				break;
			}

			stack.getItem().onItemRightClick(
					stack,
					getWorld(),
					CoreProxy.proxy.getBuildCraftPlayer((WorldServer) getWorld(),
							(int) p.x, (int) p.y, (int) p.z).get());
		} else if (getWorld().getBlock((int) p.x, (int) p.y, (int) p.z) == Blocks.air) {
			if (stack.getItem() instanceof ItemBucket) {
				Block underblock = getWorld().getBlock((int) p.x, (int) p.y - 1, (int) p.z);
				Item newBucket = Items.bucket;

				if (underblock == Blocks.water) {
					newBucket = Items.water_bucket;
				}

				if (underblock == Blocks.lava) {
					newBucket = Items.lava_bucket;
				}

				boolean rollback = false;

				if (((ItemBucket) stack.getItem()).tryPlaceContainedLiquid(getWorld(),
						(int) p.x, (int) p.y - 1, (int) p.z)) {
					rollback = true;
				} else if (newBucket != Items.bucket) {
					getWorld().setBlockToAir((int) p.x, (int) p.y - 1, (int) p.z);
					rollback = true;
				}

				if (rollback) {
					stack.stackSize = 0;
					rollbackItem(newBucket, 1, event.direction);
				}
			} else {
				stack.tryPlaceItemIntoWorld(
						CoreProxy.proxy.getBuildCraftPlayer((WorldServer) getWorld()).get(),
					getWorld(), (int) p.x, (int) p.y - 1, (int) p.z, 1, 0.0f, 0.0f, 0.0f);
			}
		} else {
			stack.tryPlaceItemIntoWorld(
					CoreProxy.proxy.getBuildCraftPlayer((WorldServer) getWorld()).get(),
					getWorld(), (int) p.x, (int) p.y, (int) p.z, 1, 0.0f, 0.0f,
					0.0f);
		}
	}

	private void rollbackItem(Item item, int quantity, ForgeDirection direction) {
		rollbackItem(new ItemStack(item, quantity), direction);
	}

	private void rollbackItem(ItemStack itemStack, ForgeDirection direction) {
		TravelingItem newItem = TravelingItem.make(
				container.xCoord + 0.5,
				container.yCoord + TransportUtils.getPipeFloorOf(itemStack),
				container.zCoord + 0.5, itemStack);
		transport.injectItem(newItem, direction.getOpposite());
	}

	@SuppressWarnings("unchecked")
	public boolean convertPipe(PipeTransportItems pipe, TravelingItem item) {
		if (item.getItemStack().getItem() instanceof ItemPipe) {
			if (!(item.getItemStack().getItem() == BuildCraftTransport.pipeItemsStripes)) {
				Pipe newPipe = BlockGenericPipe.createPipe(item.getItemStack().getItem());
				newPipe.setTile(this.container);
				this.container.pipe = newPipe;

				item.getItemStack().stackSize--;

				if (item.getItemStack().stackSize <= 0) {
					((PipeTransportItems) newPipe.transport).items.remove(item);
				}

				return true;
			}
		}

		return false;
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
	public boolean canPipeConnect(TileEntity tile, ForgeDirection side) {
		if (tile instanceof TileGenericPipe) {
			TileGenericPipe tilePipe = (TileGenericPipe) tile;

			if (tilePipe.pipe instanceof PipeItemsStripes) {
				return false;
			}
		}

		return super.canPipeConnect(tile, side);
	}
}
