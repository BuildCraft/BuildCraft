package buildcraft.transport;

import buildcraft.api.core.IIconProvider;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.util.Icon;

public class PipeIconProvider implements IIconProvider {

	public enum TYPE {

		PipeStructureCobblestone("pipeStructureCobblestone"),
		//
		PipeItemsCobbleStone("pipeItemsCobblestone"),
		//
		PipeItemsDiamond_Center("pipeItemsDiamond_center"),
		PipeItemsDiamond_Down("pipeItemsDiamond_down"),
		PipeItemsDiamond_Up("pipeItemsDiamond_up"),
		PipeItemsDiamond_North("pipeItemsDiamond_north"),
		PipeItemsDiamond_South("pipeItemsDiamond_south"),
		PipeItemsDiamond_West("pipeItemsDiamond_west"),
		PipeItemsDiamond_East("pipeItemsDiamond_east"),
		//
		PipeItemsLapis_Black("pipeItemsLapisBlack"),
		PipeItemsLapis_Red("pipeItemsLapisRed"),
		PipeItemsLapis_Green("pipeItemsLapisGreen"),
		PipeItemsLapis_Brown("pipeItemsLapisBrown"),
		PipeItemsLapis_Blue("pipeItemsLapisBlue"),
		PipeItemsLapis_Purple("pipeItemsLapisPurple"),
		PipeItemsLapis_Cyan("pipeItemsLapisCyan"),
		PipeItemsLapis_LightGray("pipeItemsLapisLightGray"),
		PipeItemsLapis_Gray("pipeItemsLapisGray"),
		PipeItemsLapis_Pink("pipeItemsLapisPink"),
		PipeItemsLapis_Lime("pipeItemsLapisLime"),
		PipeItemsLapis_Yellow("pipeItemsLapisYellow"),
		PipeItemsLapis_LightBlue("pipeItemsLapisLightBlue"),
		PipeItemsLapis_Magenta("pipeItemsLapisMagenta"),
		PipeItemsLapis_Orange("pipeItemsLapisOrange"),
		PipeItemsLapis_White("pipeItemsLapisWhite"),
		//
		PipeItemsWood_Standard("pipeItemsWood_standard"),
		PipeAllWood_Solid("pipeAllWood_solid"),
		//
		PipeItemsEmerald_Standard("pipeItemsEmerald_standard"),
		PipeAllEmerald_Solid("pipeAllEmerald_solid"),
		//
		PipeItemsGold("pipeItemsGold"),
		//
		PipeItemsIron_Standard("pipeItemsIron_standard"),
		PipeAllIron_Solid("pipeAllIron_solid"),
		//
		PipeItemsObsidian("pipeItemsObsidian"),
		PipeItemsSandstone("pipeItemsSandstone"),
		PipeItemsStone("pipeItemsStone"),
		PipeItemsQuartz("pipeItemsQuartz"),
		PipeItemsVoid("pipeItemsVoid"),
		//
		PipeFluidsCobblestone("pipeFluidsCobblestone"),
		PipeFluidsWood_Standard("pipeFluidsWood_standard"),
		PipeFluidsEmerald_Standard("pipeFluidsEmerald_standard"),
		PipeFluidsGold("pipeFluidsGold"),
		PipeFluidsIron_Standard("pipeFluidsIron_standard"),
		PipeFluidsSandstone("pipeFluidsSandstone"),
		PipeFluidsStone("pipeFluidsStone"),
		PipeFluidsVoid("pipeFluidsVoid"),
		//
		PipePowerDiamond("pipePowerDiamond"),
		PipePowerGold("pipePowerGold"),
		PipePowerQuartz("pipePowerQuartz"),
		PipePowerStone("pipePowerStone"),
		PipePowerCobblestone("pipePowerCobblestone"),
		PipePowerWood_Standard("pipePowerWood_standard"),
		//
		Power_Normal("texture_cyan"),
		Power_Overload("texture_red_lit"),
		Stripes("pipeStripes"),
		//
		ItemBox("itemBox");
		public static final TYPE[] VALUES = values();
		private final String iconTag;
		private Icon icon;

		private TYPE(String iconTag) {
			this.iconTag = iconTag;
		}

		private void registerIcon(IconRegister iconRegister) {
			icon = iconRegister.registerIcon("buildcraft:" + iconTag);
		}

		public Icon getIcon() {
			return icon;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int pipeIconIndex) {
		if (pipeIconIndex == -1)
			return null;
		return TYPE.VALUES[pipeIconIndex].icon;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconRegister) {
		for (TYPE type : TYPE.VALUES) {
			type.registerIcon(iconRegister);
		}
	}
}
