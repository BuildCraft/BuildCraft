/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.pluggable;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.transport.IPipe;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.pluggable.IPipePluggableItem;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.core.lib.items.ItemBuildCraft;
import buildcraft.core.lib.utils.ColorUtils;
import buildcraft.core.lib.utils.StringUtils;

public class ItemLens extends ItemBuildCraft implements IPipePluggableItem {
	public ItemLens() {
		super();
		setHasSubtypes(true);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamageForRenderPass(int meta, int pass) {
		switch (meta) {
			case 32:
				return pass == 0 ? icons[3] : icons[0];
			case 33:
				return pass == 0 ? icons[3] : icons[2];
			default:
				return icons[meta >= 16 ? (1 + (pass & 1)) : (1 - (pass & 1))];
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean requiresMultipleRenderPasses() {
		return true;
	}

	public int getDye(ItemStack stack) {
		return 15 - (stack.getItemDamage() & 15);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getColorFromItemStack(ItemStack stack, int pass) {
		if (stack.getItemDamage() >= 32) {
			return 16777215;
		}
		return pass == 0 ? ColorUtils.getRGBColor(getDye(stack)) : 16777215;
	}

	@Override
	public String getItemStackDisplayName(ItemStack itemstack) {
		if (itemstack.getItemDamage() >= 32) {
			return StringUtils.localize(itemstack.getItemDamage() == 33 ? "item.Filter.name" : "item.Lens.name") + " (" + StringUtils.localize("color.clear") + ")";
		} else {
			return StringUtils.localize(itemstack.getItemDamage() >= 16 ? "item.Filter.name" : "item.Lens.name") + " (" + StringUtils.localize("color." + ColorUtils.getName(getDye(itemstack))) + ")";
		}
	}

	@Override
	public boolean doesSneakBypassUse(World world, int x, int y, int z, EntityPlayer player) {
		return true;
	}

	@Override
	public String[] getIconNames() {
		return new String[]{"lens/lensFrame", "lens/transparent", "lens/filterFrame", "lens/clear"};
	}


	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List itemList) {
		for (int i = 0; i < 34; i++) {
			itemList.add(new ItemStack(item, 1, i));
		}
	}

	@Override
	public PipePluggable createPipePluggable(IPipe pipe, ForgeDirection side, ItemStack stack) {
		if (pipe.getTile().getPipeType() == IPipeTile.PipeType.ITEM) {
			return new LensPluggable(stack);
		} else {
			return null;
		}
	}
}
