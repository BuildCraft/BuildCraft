package buildcraft.builders.urbanism;

import net.minecraft.util.Icon;
import net.minecraft.util.MovingObjectPosition;

class UrbanistToolErase extends UrbanistTool {

	@Override
	public Icon getIcon() {
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