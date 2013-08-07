package buildcraft.transport;

import buildcraft.api.core.IIconProvider;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.util.Icon;

public class GateIconProvider implements IIconProvider {
	
	public static final int Gate_Dark 						=  0;
	public static final int Gate_Lit 						=  1;
	public static final int Gate_Iron_And_Dark 				=  2;
	public static final int Gate_Iron_And_Lit 				=  3;
	public static final int Gate_Iron_Or_Dark 				=  4;
	public static final int Gate_Iron_Or_Lit 				=  5;
	public static final int Gate_Gold_And_Dark 				=  6;
	public static final int Gate_Gold_And_Lit 				=  7;
	public static final int Gate_Gold_Or_Dark 				=  8;
	public static final int Gate_Gold_Or_Lit 				=  9;
	public static final int Gate_Diamond_And_Dark 			= 10;
	public static final int Gate_Diamond_And_Lit 			= 11;
	public static final int Gate_Diamond_Or_Dark 			= 12;
	public static final int Gate_Diamond_Or_Lit 			= 13;
	
	public static final int Gate_Autarchic_Dark 			= 14;
	public static final int Gate_Autarchic_Lit 				= 15;
	public static final int Gate_Autarchic_Iron_And_Dark 	= 16;
	public static final int Gate_Autarchic_Iron_And_Lit 	= 17;
	public static final int Gate_Autarchic_Iron_Or_Dark 	= 18;
	public static final int Gate_Autarchic_Iron_Or_Lit 		= 19;
	public static final int Gate_Autarchic_Gold_And_Dark 	= 20;
	public static final int Gate_Autarchic_Gold_And_Lit 	= 21;
	public static final int Gate_Autarchic_Gold_Or_Dark 	= 22;
	public static final int Gate_Autarchic_Gold_Or_Lit 		= 23;
	public static final int Gate_Autarchic_Diamond_And_Dark = 24;
	public static final int Gate_Autarchic_Diamond_And_Lit 	= 25;
	public static final int Gate_Autarchic_Diamond_Or_Dark 	= 26;
	public static final int Gate_Autarchic_Diamond_Or_Lit 	= 27;	
	
	public static final int MAX								= 28;

	@SideOnly(Side.CLIENT)
	private Icon[] icons;
	
	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int pipeIconIndex) {
		return icons[pipeIconIndex];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconRegister) {
		
		icons = new Icon[MAX];
		icons[GateIconProvider.Gate_Dark] = iconRegister.registerIcon("buildcraft:gate_dark");
		icons[GateIconProvider.Gate_Lit] = iconRegister.registerIcon("buildcraft:gate_lit");
		icons[GateIconProvider.Gate_Iron_And_Dark] = iconRegister.registerIcon("buildcraft:gate_iron_and_dark");
		icons[GateIconProvider.Gate_Iron_And_Lit] = iconRegister.registerIcon("buildcraft:gate_iron_and_lit");
		icons[GateIconProvider.Gate_Iron_Or_Dark] = iconRegister.registerIcon("buildcraft:gate_iron_or_dark");
		icons[GateIconProvider.Gate_Iron_Or_Lit] = iconRegister.registerIcon("buildcraft:gate_iron_or_lit");
		icons[GateIconProvider.Gate_Gold_And_Dark] = iconRegister.registerIcon("buildcraft:gate_gold_and_dark");
		icons[GateIconProvider.Gate_Gold_And_Lit] = iconRegister.registerIcon("buildcraft:gate_gold_and_lit");
		icons[GateIconProvider.Gate_Gold_Or_Dark] = iconRegister.registerIcon("buildcraft:gate_gold_or_dark");
		icons[GateIconProvider.Gate_Gold_Or_Lit] = iconRegister.registerIcon("buildcraft:gate_gold_or_lit");
		icons[GateIconProvider.Gate_Diamond_And_Dark] = iconRegister.registerIcon("buildcraft:gate_diamond_and_dark");
		icons[GateIconProvider.Gate_Diamond_And_Lit] = iconRegister.registerIcon("buildcraft:gate_diamond_and_lit");
		icons[GateIconProvider.Gate_Diamond_Or_Dark] = iconRegister.registerIcon("buildcraft:gate_diamond_or_dark");
		icons[GateIconProvider.Gate_Diamond_Or_Lit] = iconRegister.registerIcon("buildcraft:gate_diamond_or_lit");
		
		icons[GateIconProvider.Gate_Autarchic_Dark] = iconRegister.registerIcon("buildcraft:gate_autarchic_dark");
		icons[GateIconProvider.Gate_Autarchic_Lit] = iconRegister.registerIcon("buildcraft:gate_autarchic_lit");
		icons[GateIconProvider.Gate_Autarchic_Iron_And_Dark] = iconRegister.registerIcon("buildcraft:gate_autarchic_iron_and_dark");
		icons[GateIconProvider.Gate_Autarchic_Iron_And_Lit] = iconRegister.registerIcon("buildcraft:gate_autarchic_iron_and_lit");
		icons[GateIconProvider.Gate_Autarchic_Iron_Or_Dark] = iconRegister.registerIcon("buildcraft:gate_autarchic_iron_or_dark");
		icons[GateIconProvider.Gate_Autarchic_Iron_Or_Lit] = iconRegister.registerIcon("buildcraft:gate_autarchic_iron_or_lit");
		icons[GateIconProvider.Gate_Autarchic_Gold_And_Dark] = iconRegister.registerIcon("buildcraft:gate_autarchic_gold_and_dark");
		icons[GateIconProvider.Gate_Autarchic_Gold_And_Lit] = iconRegister.registerIcon("buildcraft:gate_autarchic_gold_and_lit");
		icons[GateIconProvider.Gate_Autarchic_Gold_Or_Dark] = iconRegister.registerIcon("buildcraft:gate_autarchic_gold_or_dark");
		icons[GateIconProvider.Gate_Autarchic_Gold_Or_Lit] = iconRegister.registerIcon("buildcraft:gate_autarchic_gold_or_lit");
		icons[GateIconProvider.Gate_Autarchic_Diamond_And_Dark] = iconRegister.registerIcon("buildcraft:gate_autarchic_diamond_and_dark");
		icons[GateIconProvider.Gate_Autarchic_Diamond_And_Lit] = iconRegister.registerIcon("buildcraft:gate_autarchic_diamond_and_lit");
		icons[GateIconProvider.Gate_Autarchic_Diamond_Or_Dark] = iconRegister.registerIcon("buildcraft:gate_autarchic_diamond_or_dark");
		icons[GateIconProvider.Gate_Autarchic_Diamond_Or_Lit] = iconRegister.registerIcon("buildcraft:gate_autarchic_diamond_or_lit");	
	}

}
