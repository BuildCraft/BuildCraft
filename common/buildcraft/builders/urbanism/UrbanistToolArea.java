package buildcraft.builders.urbanism;

import net.minecraft.util.Icon;

class UrbanistToolArea extends UrbanistTool {
	@Override
	public Icon getIcon() {
		return UrbanistToolsIconProvider.INSTANCE.getIcon(UrbanistToolsIconProvider.Tool_Area);
	}

	@Override
	public String getDescription() {
		return "Define Area";
	}
}