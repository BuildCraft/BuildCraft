/** 
 * Copyright (c) SpaceToad, 2011-2012
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.builders;

import buildcraft.BuildCraftBuilders;
import buildcraft.core.BptBase;

public class ItemBptBluePrint extends ItemBptBase {

	public ItemBptBluePrint(int i) {
		super(i);
	}

	@Override
	public int getIconFromDamage(int i) {
		BptBase bpt = BuildCraftBuilders.getBptRootIndex().getBluePrint(i);
		if (bpt == null) {
			return 5 * 16 + 2;
		} else {
			return 5 * 16 + 3;
		}
	}
}
