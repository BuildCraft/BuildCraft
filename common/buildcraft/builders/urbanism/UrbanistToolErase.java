/**
 * Copyright (c) 2011-2014, SpaceToaimport javax.swing.Icon;

import net.minecraft.util.MovingObjectPosition;
istributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.urbanism;

import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;

class UrbanistToolErase extends UrbanistTool {

	@Override
	public IIcon getIcon() {
		return UrbanistToolsIconProvider.INSTANCE.getIcon(UrbanistToolsIconProvider.Tool_Block_Erase);
	}

	@Override
	public String getDescription() {
		return "Erase Block";
	}

	@Override
	public void worldClicked (GuiUrbanist gui, MovingObjectPosition pos) {
		gui.urbanist.rpcEraseBlock(pos.blockX, pos.blockY, pos.blockZ);
	}
}