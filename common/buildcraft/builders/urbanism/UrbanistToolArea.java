/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.urbanism;

import org.lwjgl.input.Mouse;

import net.minecraft.client.Minecraft;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;

class UrbanistToolArea extends UrbanistTool {

	private int step = 0;
	private int startX, startY, startZ;
	private int x, y, z;
	private float baseY = 0;

	@Override
	public IIcon getIcon() {
		return UrbanistToolsIconProvider.INSTANCE.getIcon(UrbanistToolsIconProvider.Tool_Area);
	}

	@Override
	public String getDescription() {
		return "Define Area";
	}

	@Override
	public void worldClicked (GuiUrbanist gui, MovingObjectPosition pos) {
		if (step == 0) {
			x = pos.blockX;
			y = pos.blockY + 1;
			z = pos.blockZ;

			startX = x;
			startY = y;
			startZ = z;

			gui.urbanist.rpcCreateFrame(x, y, z);

			step = 1;
		} else if (step == 1) {
			step = 2;
			baseY = (float) Mouse.getY() / (float) Minecraft.getMinecraft().displayHeight;
		} else if (step == 2) {
			step = 0;

			areaSet (gui, startX, startY, startZ, x, y, z);
		}
	}

	public void areaSet (GuiUrbanist urbanist, int x1, int y1, int z1, int x2, int y2, int z2) {

	}

	@Override
	public void worldMoved(GuiUrbanist gui, MovingObjectPosition pos) {
		if (step == 1) {
			x = pos.blockX;
			z = pos.blockZ;

			gui.urbanist.rpcMoveFrame(x, y, z);
		} else if (step == 2) {
			float ydiff = (float) Mouse.getY() / (float) Minecraft.getMinecraft().displayHeight;

			y = (int) (startY + (ydiff - baseY) * 50);

			gui.urbanist.rpcMoveFrame(x, y, z);
		}
	}
}