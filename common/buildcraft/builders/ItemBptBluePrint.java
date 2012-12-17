/** 
 * Copyright (c) SpaceToad, 2011-2012
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.builders;

public class ItemBptBluePrint extends ItemBptBase {

	public ItemBptBluePrint(int i) {
		super(i);
	}

	@Override
	public int getIconFromDamage(int i) {
		if (i == 0)
			return 5 * 16 + 2;
		else
			return 5 * 16 + 3;
	}
}
