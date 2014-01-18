package buildcraft.builders.urbanism;

import net.minecraft.util.Icon;

class UrbanistToolBlueprint extends UrbanistTool {
	@Override
	public Icon getIcon() {
		return UrbanistToolsIconProvider.INSTANCE.getIcon(UrbanistToolsIconProvider.Tool_Blueprint);
	}

	@Override
	public String getDescription() {
		return "Build from Blueprint";
	}
}