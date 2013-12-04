package buildcraft.transport;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.util.Icon;
import buildcraft.BuildCraftCore;
import buildcraft.api.core.IIconProvider;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PipeIconProvider implements IIconProvider {

	public enum TYPE {

		PipeStructureCobblestone("pipeStructureCobblestone"),
		//
		PipeItemsCobbleStone("pipeItemsCobblestone"),
		//
		PipeItemsDiamond_Item("pipeItemsDiamond_item"),
		PipeItemsDiamond_Center("pipeItemsDiamond_center"),
		PipeItemsDiamond_Down("pipeItemsDiamond_down"),
		PipeItemsDiamond_Up("pipeItemsDiamond_up"),
		PipeItemsDiamond_North("pipeItemsDiamond_north"),
		PipeItemsDiamond_South("pipeItemsDiamond_south"),
		PipeItemsDiamond_West("pipeItemsDiamond_west", "pipeItemsDiamond_west_cb"),
		PipeItemsDiamond_East("pipeItemsDiamond_east"),
		//
		PipeItemsLapis_Black("pipeItemsLapis_black"),
		PipeItemsLapis_Red("pipeItemsLapis_red"),
		PipeItemsLapis_Green("pipeItemsLapis_green"),
		PipeItemsLapis_Brown("pipeItemsLapis_brown"),
		PipeItemsLapis_Blue("pipeItemsLapis_blue"),
		PipeItemsLapis_Purple("pipeItemsLapis_purple"),
		PipeItemsLapis_Cyan("pipeItemsLapis_cyan"),
		PipeItemsLapis_LightGray("pipeItemsLapis_lightgray"),
		PipeItemsLapis_Gray("pipeItemsLapis_gray"),
		PipeItemsLapis_Pink("pipeItemsLapis_pink"),
		PipeItemsLapis_Lime("pipeItemsLapis_lime"),
		PipeItemsLapis_Yellow("pipeItemsLapis_yellow"),
		PipeItemsLapis_LightBlue("pipeItemsLapis_lightblue"),
		PipeItemsLapis_Magenta("pipeItemsLapis_magenta"),
		PipeItemsLapis_Orange("pipeItemsLapis_orange"),
		PipeItemsLapis_White("pipeItemsLapis_white"),
		//
		PipeItemsDaizuli_Black("pipeItemsDaizuli_black"),
		PipeItemsDaizuli_Red("pipeItemsDaizuli_red"),
		PipeItemsDaizuli_Green("pipeItemsDaizuli_green"),
		PipeItemsDaizuli_Brown("pipeItemsDaizuli_brown"),
		PipeItemsDaizuli_Blue("pipeItemsDaizuli_blue"),
		PipeItemsDaizuli_Purple("pipeItemsDaizuli_purple"),
		PipeItemsDaizuli_Cyan("pipeItemsDaizuli_cyan"),
		PipeItemsDaizuli_LightGray("pipeItemsDaizuli_lightgray"),
		PipeItemsDaizuli_Gray("pipeItemsDaizuli_gray"),
		PipeItemsDaizuli_Pink("pipeItemsDaizuli_pink"),
		PipeItemsDaizuli_Lime("pipeItemsDaizuli_lime"),
		PipeItemsDaizuli_Yellow("pipeItemsDaizuli_yellow"),
		PipeItemsDaizuli_LightBlue("pipeItemsDaizuli_lightblue"),
		PipeItemsDaizuli_Magenta("pipeItemsDaizuli_magenta"),
		PipeItemsDaizuli_Orange("pipeItemsDaizuli_orange"),
		PipeItemsDaizuli_White("pipeItemsDaizuli_white"),
		PipeAllDaizuli_Solid("pipeAllDaizuli_solid"),
		//
		PipeItemsWood_Standard("pipeItemsWood_standard"),
		PipeAllWood_Solid("pipeAllWood_solid"),
		//
		PipeItemsEmerald_Standard("pipeItemsEmerald_standard"),
		PipeAllEmerald_Solid("pipeAllEmerald_solid"),
		//
		PipeItemsEmzuli_Standard("pipeItemsEmzuli_standard"),
		PipeAllEmzuli_Solid("pipeAllEmzuli_solid"),
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
		PipePowerIronM2("pipePowerIronM2"),
		PipePowerIronM4("pipePowerIronM4"),
		PipePowerIronM8("pipePowerIronM8"),
		PipePowerIronM16("pipePowerIronM16"),
		PipePowerIronM32("pipePowerIronM32"),
		PipePowerIronM64("pipePowerIronM64"),
		PipePowerIronM128("pipePowerIronM128"),
		//
		Power_Normal("texture_cyan"),
		Power_Overload("texture_red_lit"),
		Stripes("pipeStripes"),
		//
		ItemBox("itemBox");
		public static final TYPE[] VALUES = values();
		private final String iconTag;
		private final String iconTagColorBlind;
		private Icon icon;

		private TYPE(String iconTag, String IconTagColorBlind) {
			this.iconTag = iconTag;
			this.iconTagColorBlind = IconTagColorBlind;
		}

		private TYPE(String iconTag) {
			this(iconTag, iconTag);
		}

		private void registerIcon(IconRegister iconRegister) {
			icon = iconRegister.registerIcon("buildcraft:" + (BuildCraftCore.colorBlindMode ? iconTagColorBlind : iconTag));
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
