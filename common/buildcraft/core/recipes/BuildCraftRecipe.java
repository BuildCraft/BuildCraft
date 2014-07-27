/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.recipes;

import java.lang.reflect.Field;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import net.minecraftforge.oredict.ShapedOreRecipe;

import buildcraft.core.science.Technology;

public class BuildCraftRecipe extends ShapedOreRecipe {

	Technology techno;

	public BuildCraftRecipe(Technology iTechno, ItemStack result, Object[] recipe) {
		super(result, recipe);

		techno = iTechno;
	}

	@Override
	public boolean matches(InventoryCrafting inv, World world) {
		try {
			Field f = InventoryCrafting.class.getDeclaredField("eventHandler");

			if (!f.isAccessible()) {
				f.setAccessible(true);
			}

			Container container = (Container) f.get(inv);

			f = Container.class.getDeclaredField("crafters");

			if (!f.isAccessible()) {
				f.setAccessible(true);
			}

			List crafters = (List) f.get(container);

			for (Object p : crafters) {
				EntityPlayer player = (EntityPlayer) p;

				System.out.println(player.getEntityId());
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}

		return super.matches(inv, world);
	}
}
