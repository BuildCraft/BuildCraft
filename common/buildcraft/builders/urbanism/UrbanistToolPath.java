package buildcraft.builders.urbanism;

import net.minecraft.util.Icon;

class UrbanistToolPath extends UrbanistTool {
	@Override
	public Icon getIcon() {
		return UrbanistToolsIconProvider.INSTANCE.getIcon(UrbanistToolsIconProvider.Tool_Path);
	}

	@Override
	public String getDescription() {
		return "Define Path";
	}
}