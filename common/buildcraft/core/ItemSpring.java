/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.core;

import java.util.Locale;
import net.minecraft.item.ItemStack;

public class ItemSpring extends ItemBlockBuildCraft {

	public ItemSpring(int i) {
		super(i);
		setMaxDamage(0);
		setHasSubtypes(true);
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return "tile.spring."+ BlockSpring.EnumSpring.fromMeta(stack.getItemDamage()).name().toLowerCase(Locale.ENGLISH);
	}
}
