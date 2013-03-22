package buildcraft.transport;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.util.Icon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import buildcraft.api.core.IIconProvider;

public class PipeIconProvider implements IIconProvider {
	
	public static final int PipeStructureCobblestone 		=  0;
	public static final int PipeItemsCobbleStone 			=  1;
	public static final int PipeItemsDiamond_Center 		=  2;
	public static final int PipeItemsDiamond_Down 			=  3;
	public static final int PipeItemsDiamond_Up 			=  4;
	public static final int PipeItemsDiamond_North 			=  5;
	public static final int PipeItemsDiamond_South 			=  6;
	public static final int PipeItemsDiamond_West 			=  7;
	public static final int PipeItemsDiamond_East 			=  8;
	public static final int PipeItemsWood_Standard			=  9;
	public static final int PipeAllWood_Solid				= 10;
	public static final int PipeItemsEmerald_Standard 		= 11;
	public static final int PipeAllEmerald_Solid 			= 12;
	public static final int PipeItemsGold 					= 13;
	public static final int PipeItemsIron_Standard 			= 14;
	public static final int PipeAllIron_Solid 				= 15;
	public static final int PipeItemsObsidian 				= 16;
	public static final int PipeItemsSandstone 				= 17;	
	public static final int PipeItemsStone 					= 18;
	public static final int PipeItemsVoid 					= 19;
	public static final int PipeLiquidsCobblestone 			= 20;
	public static final int PipeLiquidsWood_Standard 		= 21;
	public static final int PipeLiquidsEmerald_Standard 	= 22;
	public static final int PipeLiquidsGold 				= 23;
	public static final int PipeLiquidsIron_Standard 		= 24;
	public static final int PipeLiquidsSandstone 			= 25;
	public static final int PipeLiquidsStone 				= 26;
	public static final int PipeLiquidsVoid 				= 27;
	public static final int PipePowerGold 					= 28;
	public static final int PipePowerStone 					= 29;
	public static final int PipePowerWood_Standard 			= 30;
	
	public static final int Power_Normal		 			= 31;
	public static final int Power_Overload		 			= 32;
	
	public static final int MAX								= 33;
	
	private boolean registered = false;
	
	@SideOnly(Side.CLIENT)
	private Icon[] _icons;

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int pipeIconIndex) {
		return _icons[pipeIconIndex];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconRegister) {
		if (registered) return;
		registered = true;
		
		_icons = new Icon[PipeIconProvider.MAX];
		
		_icons[PipeIconProvider.PipeStructureCobblestone] = iconRegister.registerIcons("buildcraft:pipeStructureCobblestone");
		
		_icons[PipeIconProvider.PipeItemsCobbleStone] = iconRegister.registerIcons("buildcraft:pipeItemsCobblestone");
		
		_icons[PipeIconProvider.PipeItemsDiamond_Center] = iconRegister.registerIcons("buildcraft:pipeItemsDiamond_center");
		_icons[PipeIconProvider.PipeItemsDiamond_Down] = iconRegister.registerIcons("buildcraft:pipeItemsDiamond_down");
		_icons[PipeIconProvider.PipeItemsDiamond_Up] = iconRegister.registerIcons("buildcraft:pipeItemsDiamond_up");
		_icons[PipeIconProvider.PipeItemsDiamond_North] = iconRegister.registerIcons("buildcraft:pipeItemsDiamond_north");
		_icons[PipeIconProvider.PipeItemsDiamond_South] = iconRegister.registerIcons("buildcraft:pipeItemsDiamond_south");
		_icons[PipeIconProvider.PipeItemsDiamond_West] = iconRegister.registerIcons("buildcraft:pipeItemsDiamond_west");
		_icons[PipeIconProvider.PipeItemsDiamond_East] = iconRegister.registerIcons("buildcraft:pipeItemsDiamond_east");
		
		_icons[PipeIconProvider.PipeItemsWood_Standard] = iconRegister.registerIcons("buildcraft:pipeItemsWood_standard");
		_icons[PipeIconProvider.PipeAllWood_Solid] = iconRegister.registerIcons("buildcraft:pipeAllWood_solid");
		
		_icons[PipeIconProvider.PipeItemsEmerald_Standard] = iconRegister.registerIcons("buildcraft:pipeItemsEmerald_standard");
		_icons[PipeIconProvider.PipeAllEmerald_Solid] = iconRegister.registerIcons("buildcraft:pipeAllEmerald_solid");
		
		_icons[PipeIconProvider.PipeItemsGold] = iconRegister.registerIcons("buildcraft:pipeItemsGold");
		
		_icons[PipeIconProvider.PipeItemsIron_Standard] = iconRegister.registerIcons("buildcraft:pipeItemsIron_standard");
		_icons[PipeIconProvider.PipeAllIron_Solid] = iconRegister.registerIcons("buildcraft:pipeAllIron_solid");
		
		_icons[PipeIconProvider.PipeItemsObsidian] = iconRegister.registerIcons("buildcraft:pipeItemsObsidian");
		_icons[PipeIconProvider.PipeItemsSandstone] = iconRegister.registerIcons("buildcraft:pipeItemsSandstone");
		_icons[PipeIconProvider.PipeItemsStone] = iconRegister.registerIcons("buildcraft:pipeItemsStone");
		_icons[PipeIconProvider.PipeItemsVoid] = iconRegister.registerIcons("buildcraft:pipeItemsVoid");
		
		_icons[PipeIconProvider.PipeLiquidsCobblestone] = iconRegister.registerIcons("buildcraft:pipeLiquidsCobblestone");
		_icons[PipeIconProvider.PipeLiquidsWood_Standard] = iconRegister.registerIcons("buildcraft:pipeLiquidsWood_standard");
		_icons[PipeIconProvider.PipeLiquidsEmerald_Standard] = iconRegister.registerIcons("buildcraft:pipeLiquidsEmerald_standard");
		_icons[PipeIconProvider.PipeLiquidsGold] = iconRegister.registerIcons("buildcraft:pipeLiquidsGold");
		_icons[PipeIconProvider.PipeLiquidsIron_Standard] = iconRegister.registerIcons("buildcraft:pipeLiquidsIron_standard");
		_icons[PipeIconProvider.PipeLiquidsSandstone] = iconRegister.registerIcons("buildcraft:pipeLiquidsSandstone");
		_icons[PipeIconProvider.PipeLiquidsStone] = iconRegister.registerIcons("buildcraft:pipeLiquidsStone");
		_icons[PipeIconProvider.PipeLiquidsVoid] = iconRegister.registerIcons("buildcraft:pipeLiquidsVoid");
		
		_icons[PipeIconProvider.PipePowerGold] = iconRegister.registerIcons("buildcraft:pipePowerGold");
		_icons[PipeIconProvider.PipePowerStone] = iconRegister.registerIcons("buildcraft:pipePowerStone");
		_icons[PipeIconProvider.PipePowerWood_Standard] = iconRegister.registerIcons("buildcraft:pipePowerWood_standard");
		
		_icons[PipeIconProvider.Power_Normal] = iconRegister.registerIcons("buildcraft:texture_cyan");
		_icons[PipeIconProvider.Power_Overload] = iconRegister.registerIcons("buildcraft:texture_red_dark");
	}

}
