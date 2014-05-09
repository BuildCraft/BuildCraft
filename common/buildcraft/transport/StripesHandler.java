/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLeavesBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.Position;
import buildcraft.api.transport.IStripesHandler;
import buildcraft.api.transport.IStripesPipe;
import buildcraft.core.proxy.CoreProxy;

public class StripesHandler implements IStripesHandler {

	@Override
	public boolean handleStripesEvent(World world, ItemStack itemStack, Position p, IStripesPipe pipe) {
		EntityPlayer player = CoreProxy.proxy.getBuildCraftPlayer(world, (int) p.x, (int) p.y, (int) p.z);
		setupRotation(player, p.orientation);
		Item pItem = itemStack.getItem();
		Block block = world.getBlock((int) p.x, (int) p.y, (int) p.z);

		if (pItem instanceof ItemPipe) {
			if (!(pItem == BuildCraftTransport.pipeItemsStripes)) {
				Pipe newPipe = BlockGenericPipe.createPipe(itemStack.getItem());
				newPipe.setTile(pipe.getContainer());
				pipe.setPipeContainer(newPipe);
				itemStack.stackSize--;
				return BuildCraftTransport.pipeItemsStripes.onItemUse(new ItemStack(
								BuildCraftTransport.pipeItemsStripes), player, world,
						(int) p.x, (int) p.y, (int) p.z, 1, 0, 0, 0
				);
			}
		} else if (pItem instanceof ItemBlock) {
			if (block == Blocks.air) {
				return itemStack.tryPlaceItemIntoWorld(player, world, (int) p.x, (int) p.y, (int) p.z, 1, 0.0f, 0.0f, 0.0f);
			}
		} else if (pItem == Items.shears) {
			if (block instanceof BlockLeavesBase) {
				world.playSoundEffect((int) p.x, (int) p.y, (int) p.z, Block.soundTypeGrass.getBreakSound(), 1, 1);
				world.setBlockToAir((int) p.x, (int) p.y, (int) p.z);
				itemStack.damageItem(1, CoreProxy.proxy.getBuildCraftPlayer(world));
				return true;
			}
		} else if (pItem == Items.arrow) {
			ForgeDirection direction = p.orientation;
			EntityArrow entityArrow = new EntityArrow(world, player, 0);
			entityArrow.setDamage(3);
			entityArrow.setKnockbackStrength(1);
			entityArrow.posX = p.x + 0.5d;
			entityArrow.posY = p.y + 0.5d;
			entityArrow.posZ = p.z + 0.5d;
			entityArrow.motionX = direction.offsetX * 1.8d + world.rand.nextGaussian() * 0.007499999832361937D;
			entityArrow.motionY = direction.offsetY * 1.8d + world.rand.nextGaussian() * 0.007499999832361937D;
			entityArrow.motionZ = direction.offsetZ * 1.8d + world.rand.nextGaussian() * 0.007499999832361937D;
			world.spawnEntityInWorld(entityArrow);

			itemStack.stackSize--;
			pipe.rollbackItem(itemStack, p.orientation);
			return true;
		} else if ((pItem == Items.potionitem && ItemPotion.isSplash(itemStack.getItemDamage()))
				|| pItem == Items.egg || pItem == Items.snowball) {
			pItem.onItemRightClick(itemStack, world, player);
			return true;
		} else if (FluidContainerRegistry.isEmptyContainer(itemStack)) {
			Position fluidPosition = new Position(p);
			fluidPosition.moveDown(1);
			Block fluidBlock = world.getBlock((int) fluidPosition.x, (int) fluidPosition.y, (int) fluidPosition.z);
			Fluid fluid = FluidRegistry.lookupFluidForBlock(fluidBlock);
			if (fluid != null) {
				world.setBlockToAir((int) fluidPosition.x, (int) fluidPosition.y, (int) fluidPosition.z);

				FluidStack fluidStack = new FluidStack(fluid, 1000);
				ItemStack fluidContainer = FluidContainerRegistry.fillFluidContainer(fluidStack, itemStack);
				itemStack.stackSize--;
				pipe.rollbackItem(fluidContainer, p.orientation);
				return true;
			}
		} else if (FluidContainerRegistry.isFilledContainer(itemStack)) {
			Position fluidPosition = new Position(p);
			fluidPosition.moveDown(1);
			Block fluidBlock = world.getBlock((int) fluidPosition.x, (int) fluidPosition.y, (int) fluidPosition.z);

			Fluid fluidContained = FluidRegistry.lookupFluidForBlock(fluidBlock);
			FluidStack fluidStack = FluidContainerRegistry.getFluidForFilledItem(itemStack);
			Fluid fluid = fluidStack.getFluid();
			if (fluidStack.amount >= 1000 && fluid.canBePlacedInWorld()) {
				ItemStack rollbackContainer;
				if (pItem == Items.water_bucket || pItem == Items.lava_bucket) {
					rollbackContainer = new ItemStack(Items.bucket, 1, 0);
				} else {
					rollbackContainer = new ItemStack(pItem, 1, 0);
				}
				if (fluidContained != null) {
					rollbackContainer = FluidContainerRegistry.fillFluidContainer(new FluidStack(fluidContained, 1000), rollbackContainer);
				}
				pipe.rollbackItem(rollbackContainer, p.orientation);

				world.setBlock((int) fluidPosition.x, (int) fluidPosition.y, (int) fluidPosition.z, fluid.getBlock());
				return true;
			}
		}

		return false;
	}


	private static void setupRotation(EntityPlayer player, ForgeDirection direction) {
		switch (direction) {
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
		}
	}
}
