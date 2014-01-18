package buildcraft.builders.urbanism;

import net.minecraft.util.Icon;

class UrbanistToolFiller extends UrbanistTool {
	@Override
	public Icon getIcon() {
		return UrbanistToolsIconProvider.INSTANCE.getIcon(UrbanistToolsIconProvider.Tool_Filler);
	}

	@Override
	public String getDescription() {
		return "Fill";
	}
}