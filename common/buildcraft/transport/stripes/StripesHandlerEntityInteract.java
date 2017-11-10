/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.stripes;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandler;

public class StripesHandlerEntityInteract implements IStripesHandler {

	@Override
	public StripesHandlerType getType() {
		return StripesHandlerType.ITEM_USE;
	}

	@Override
	public boolean shouldHandle(ItemStack stack) {
		return true;
	}

	@Override
	public boolean handle(World world, int x, int y, int z,
						  ForgeDirection direction, ItemStack stack, EntityPlayer player,
						  IStripesActivator activator) {

		AxisAlignedBB box = AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 1, z + 1);
		List<?> entities = world.getEntitiesWithinAABBExcludingEntity(null, box);
		if (entities.size() <= 0) {
			return false;
		}

		List<EntityLivingBase> livingEntities = new LinkedList<EntityLivingBase>();
		for (Object entityObj : entities) {
			if (entityObj instanceof EntityLivingBase) {
				livingEntities.add((EntityLivingBase) entityObj);
			}
		}

		player.setCurrentItemOrArmor(0, stack);

		boolean successful = false;
		Collections.shuffle(livingEntities);
		while (livingEntities.size() > 0) {
			EntityLivingBase entity = livingEntities.remove(0);

			if (!player.interactWith(entity)) {
				continue;
			}
			successful = true;
			dropItemsExcept(stack, player, activator, direction);
		}
		if (stack.stackSize > 0 && successful) {
			activator.sendItem(stack, direction.getOpposite());
		}

		player.setCurrentItemOrArmor(0, null);

		return successful;
	}

	private void dropItemsExcept(ItemStack stack, EntityPlayer player, IStripesActivator activator, ForgeDirection direction) {
		for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
			ItemStack invStack = player.inventory.getStackInSlot(i);
			if (invStack != null && invStack != stack) {
				player.inventory.setInventorySlotContents(i, null);
				activator.sendItem(invStack, direction.getOpposite());
			}
		}
	}

}
