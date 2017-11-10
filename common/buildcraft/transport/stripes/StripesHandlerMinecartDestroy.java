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

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityMinecartContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandler;

public class StripesHandlerMinecartDestroy implements IStripesHandler {

	@Override
	public StripesHandlerType getType() {
		return StripesHandlerType.BLOCK_BREAK;
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

		List<EntityMinecart> minecarts = new LinkedList<EntityMinecart>();
		for (Object entityObj : entities) {
			if (entityObj instanceof EntityMinecart) {
				minecarts.add((EntityMinecart) entityObj);
			}
		}

		if (minecarts.size() > 0) {
			Collections.shuffle(minecarts);
			EntityMinecart cart = minecarts.get(0);
			if (cart instanceof EntityMinecartContainer) {
				// good job, Mojang. :<
				EntityMinecartContainer container = (EntityMinecartContainer) cart;
				for (int i = 0; i < container.getSizeInventory(); i++) {
					ItemStack s = container.getStackInSlot(i);
					if (s != null) {
						container.setInventorySlotContents(i, null);
						// Safety check
						if (container.getStackInSlot(i) == null) {
							activator.sendItem(s, direction.getOpposite());
						}
					}
				}
			}
			/* cart.captureDrops = true;
			cart.killMinecart(DamageSource.generic);
			for (EntityItem s : cart.capturedDrops) {
				activator.sendItem(s.getEntityItem(), direction.getOpposite());
			} */
			cart.setDead();
			activator.sendItem(cart.getCartItem(), direction.getOpposite());
			return true;
		}

		return false;
	}
}
