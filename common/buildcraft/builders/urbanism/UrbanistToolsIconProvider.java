package buildcraft.builders.urbanism;

import buildcraft.api.core.IIconProvider;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.util.Icon;

public class UrbanistToolsIconProvider implements IIconProvider {

	public static UrbanistToolsIconProvider INSTANCE = new UrbanistToolsIconProvider();
	public static final int Tool_Block_Place = 0;
	public static final int Tool_Block_Erase = 1;
	public static final int Tool_Area = 2;
	public static final int Tool_Path = 3;
	public static final int Tool_Filler = 4;
	public static final int Tool_Blueprint = 5;

	public static final int MAX = 6;
	@SideOnly(Side.CLIENT)
	private final Icon[] icons = new Icon[MAX];

	private UrbanistToolsIconProvider() {
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int iconIndex) {
		return icons[iconIndex];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconRegister) {
		icons[UrbanistToolsIconProvider.Tool_Block_Place] = iconRegister.registerIcon("buildcraft:icons/urbanist_block");
		icons[UrbanistToolsIconProvider.Tool_Block_Erase] = iconRegister.registerIcon("buildcraft:icons/urbanist_erase");
		icons[UrbanistToolsIconProvider.Tool_Area] = iconRegister.registerIcon("buildcraft:icons/urbanist_area");
		icons[UrbanistToolsIconProvider.Tool_Path] = iconRegister.registerIcon("buildcraft:icons/urbanist_path");
		icons[UrbanistToolsIconProvider.Tool_Filler] = iconRegister.registerIcon("buildcraft:icons/urbanist_filler");
		icons[UrbanistToolsIconProvider.Tool_Blueprint] = iconRegister.registerIcon("buildcraft:icons/urbanist_blueprint");
	}
}
