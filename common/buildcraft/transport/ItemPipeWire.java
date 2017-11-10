/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import buildcraft.api.transport.PipeWire;
import buildcraft.core.lib.items.ItemBuildCraft;

public class ItemPipeWire extends ItemBuildCraft {

	private IIcon[] icons;

	public ItemPipeWire() {
		super();
		setHasSubtypes(true);
		setMaxDamage(0);
		setPassSneakClick(true);
		setUnlocalizedName("pipeWire");
	}

	@Override
	public IIcon getIconFromDamage(int damage) {
		return icons[damage % icons.length];
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return "item." + PipeWire.fromOrdinal(stack.getItemDamage()).getTag();
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List itemList) {
		for (PipeWire pipeWire : PipeWire.VALUES) {
			itemList.add(pipeWire.getStack());
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister) {
		icons = new IIcon[PipeWire.VALUES.length];
		for (PipeWire pipeWire : PipeWire.VALUES) {
			icons[pipeWire.ordinal()] = par1IconRegister.registerIcon("buildcrafttransport:pipeWire/" + pipeWire.getColor().toLowerCase());
		}
	}

	public void registerItemStacks() {
		for (PipeWire pipeWire : PipeWire.VALUES) {
			GameRegistry.registerCustomItemStack(pipeWire.getTag(), pipeWire.getStack());
		}
	}
}
