package buildcraft.builders.urbanism;

import org.lwjgl.input.Mouse;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Icon;
import net.minecraft.util.MovingObjectPosition;

class UrbanistToolArea extends UrbanistTool {
	@Override
	public Icon getIcon() {
		return UrbanistToolsIconProvider.INSTANCE.getIcon(UrbanistToolsIconProvider.Tool_Area);
	}

	@Override
	public String getDescription() {
		return "Define Area";
	}

	int step = 0;
	int startX, startY, startZ;
	int x, y, z;
	float baseY = 0;

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

			areaSet (startX, startY, startZ, x, y, z);
		}
	}

	public void areaSet (int x1, int y1, int z1, int x2, int y2, int z2) {

	}

	@Override
	public void worldMoved(GuiUrbanist gui, MovingObjectPosition pos) {
		if (step == 1) {
			gui.urbanist.rpcMoveFrame(pos.blockX, y, pos.blockZ);

			x = pos.blockX;
			z = pos.blockZ;
		} else if (step == 2) {
			float ydiff = (float) Mouse.getY() / (float) Minecraft.getMinecraft().displayHeight;

			System.out.println (ydiff);

			y = (int) (startY + (ydiff - baseY) * 50);

			gui.urbanist.rpcMoveFrame(x, y, z);
		}
	}
}