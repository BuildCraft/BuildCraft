/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import buildcraft.api.transport.PipeWire;
import buildcraft.core.ItemBuildCraft;
import buildcraft.core.utils.ModelHelper;

public class ItemPipeWire extends ItemBuildCraft {

	public ItemPipeWire() {
		super();
		setHasSubtypes(true);
		setMaxDamage(0);
		setPassSneakClick(true);
		setUnlocalizedName("pipeWire");
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
	public void registerModels() {
		for (PipeWire pipeWire : PipeWire.VALUES) {
			ModelHelper.registerItemModel(this, pipeWire.ordinal(), pipeWire.getColor());
		}
	}

	public void registerItemStacks() {
		for (PipeWire pipeWire : PipeWire.VALUES) {
			//TODO (1.8): probably use Variants (not sure)
			//GameRegistry.registerCustomItemStack(pipeWire.getTag(), pipeWire.getStack());
		}
	}
}
