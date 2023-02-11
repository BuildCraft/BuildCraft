package buildcraft.transport;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.util.Icon;
import buildcraft.api.core.IIconProvider;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

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
	
	public static final int Stripes 						= 33;
	
	public static final int PipePowerCobblestone 			= 34;
	public static final int PipePowerDiamond	 			= 35;
	public static final int PipePowerQuartz	 			    = 36;
	public static final int PipeItemsQuartz	 			    = 37;
	public static final int PipeItemsClay	 			    = 38;
	public static final int PipeLiquidsClay	 			    = 39;
	
	public static final int MAX								= 40;
	
		
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
		
		_icons = new Icon[PipeIconProvider.MAX];
		
		_icons[PipeIconProvider.PipeStructureCobblestone] = iconRegister.registerIcon("buildcraft:pipeStructureCobblestone");
		
		_icons[PipeIconProvider.PipeItemsCobbleStone] = iconRegister.registerIcon("buildcraft:pipeItemsCobblestone");
		
		_icons[PipeIconProvider.PipeItemsDiamond_Center] = iconRegister.registerIcon("buildcraft:pipeItemsDiamond_center");
		_icons[PipeIconProvider.PipeItemsDiamond_Down] = iconRegister.registerIcon("buildcraft:pipeItemsDiamond_down");
		_icons[PipeIconProvider.PipeItemsDiamond_Up] = iconRegister.registerIcon("buildcraft:pipeItemsDiamond_up");
		_icons[PipeIconProvider.PipeItemsDiamond_North] = iconRegister.registerIcon("buildcraft:pipeItemsDiamond_north");
		_icons[PipeIconProvider.PipeItemsDiamond_South] = iconRegister.registerIcon("buildcraft:pipeItemsDiamond_south");
		_icons[PipeIconProvider.PipeItemsDiamond_West] = iconRegister.registerIcon("buildcraft:pipeItemsDiamond_west");
		_icons[PipeIconProvider.PipeItemsDiamond_East] = iconRegister.registerIcon("buildcraft:pipeItemsDiamond_east");
		
		_icons[PipeIconProvider.PipeItemsWood_Standard] = iconRegister.registerIcon("buildcraft:pipeItemsWood_standard");
		_icons[PipeIconProvider.PipeAllWood_Solid] = iconRegister.registerIcon("buildcraft:pipeAllWood_solid");
		
		_icons[PipeIconProvider.PipeItemsEmerald_Standard] = iconRegister.registerIcon("buildcraft:pipeItemsEmerald_standard");
		_icons[PipeIconProvider.PipeAllEmerald_Solid] = iconRegister.registerIcon("buildcraft:pipeAllEmerald_solid");
		
		_icons[PipeIconProvider.PipeItemsGold] = iconRegister.registerIcon("buildcraft:pipeItemsGold");
		
		_icons[PipeIconProvider.PipeItemsIron_Standard] = iconRegister.registerIcon("buildcraft:pipeItemsIron_standard");
		_icons[PipeIconProvider.PipeAllIron_Solid] = iconRegister.registerIcon("buildcraft:pipeAllIron_solid");
		
		_icons[PipeIconProvider.PipeItemsObsidian] = iconRegister.registerIcon("buildcraft:pipeItemsObsidian");
		_icons[PipeIconProvider.PipeItemsSandstone] = iconRegister.registerIcon("buildcraft:pipeItemsSandstone");
		_icons[PipeIconProvider.PipeItemsStone] = iconRegister.registerIcon("buildcraft:pipeItemsStone");
		_icons[PipeIconProvider.PipeItemsQuartz] = iconRegister.registerIcon("buildcraft:pipeItemsQuartz");
		_icons[PipeIconProvider.PipeItemsVoid] = iconRegister.registerIcon("buildcraft:pipeItemsVoid");
		_icons[PipeIconProvider.PipeItemsClay] = iconRegister.registerIcon("buildcraft:pipeItemsClay");
		
		_icons[PipeIconProvider.PipeLiquidsCobblestone] = iconRegister.registerIcon("buildcraft:pipeLiquidsCobblestone");
		_icons[PipeIconProvider.PipeLiquidsWood_Standard] = iconRegister.registerIcon("buildcraft:pipeLiquidsWood_standard");
		_icons[PipeIconProvider.PipeLiquidsEmerald_Standard] = iconRegister.registerIcon("buildcraft:pipeLiquidsEmerald_standard");
		_icons[PipeIconProvider.PipeLiquidsGold] = iconRegister.registerIcon("buildcraft:pipeLiquidsGold");
		_icons[PipeIconProvider.PipeLiquidsIron_Standard] = iconRegister.registerIcon("buildcraft:pipeLiquidsIron_standard");
		_icons[PipeIconProvider.PipeLiquidsSandstone] = iconRegister.registerIcon("buildcraft:pipeLiquidsSandstone");
		_icons[PipeIconProvider.PipeLiquidsStone] = iconRegister.registerIcon("buildcraft:pipeLiquidsStone");
		_icons[PipeIconProvider.PipeLiquidsVoid] = iconRegister.registerIcon("buildcraft:pipeLiquidsVoid");
		_icons[PipeIconProvider.PipeLiquidsClay] = iconRegister.registerIcon("buildcraft:pipeLiquidsClay");
		
		_icons[PipeIconProvider.PipePowerDiamond] = iconRegister.registerIcon("buildcraft:pipePowerDiamond");
		_icons[PipeIconProvider.PipePowerGold] = iconRegister.registerIcon("buildcraft:pipePowerGold");
		_icons[PipeIconProvider.PipePowerQuartz] = iconRegister.registerIcon("buildcraft:pipePowerQuartz");
		_icons[PipeIconProvider.PipePowerStone] = iconRegister.registerIcon("buildcraft:pipePowerStone");
		_icons[PipeIconProvider.PipePowerCobblestone] = iconRegister.registerIcon("buildcraft:pipePowerCobblestone");
		_icons[PipeIconProvider.PipePowerWood_Standard] = iconRegister.registerIcon("buildcraft:pipePowerWood_standard");		
		
		_icons[PipeIconProvider.Power_Normal] = iconRegister.registerIcon("buildcraft:texture_cyan");
		_icons[PipeIconProvider.Power_Overload] = iconRegister.registerIcon("buildcraft:texture_red_lit");
		_icons[PipeIconProvider.Stripes] = iconRegister.registerIcon("buildcraft:pipeStripes");
	}

}
