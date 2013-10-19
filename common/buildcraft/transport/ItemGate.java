package buildcraft.transport;

import buildcraft.api.gates.ActionManager;
import buildcraft.api.gates.IAction;
import buildcraft.api.gates.ITrigger;
import buildcraft.core.ItemBuildCraft;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;

public class ItemGate extends ItemBuildCraft {

	public static final int Gate								=  0;
	public static final int Gate_Iron_And						=  1;
	public static final int Gate_Iron_Or						=  2;
	public static final int Gate_Gold_And						=  3;
	public static final int Gate_Gold_Or						=  4;
	public static final int Gate_Diamond_And					=  5;
	public static final int Gate_Diamond_Or						=  6;

	public static final int Autarchic_Gate						=  7;
	public static final int Autarchic_Gate_Iron_And				=  8;
	public static final int Autarchic_Gate_Iron_Or				=  9;
	public static final int Autarchic_Gate_Gold_And				= 10;
	public static final int Autarchic_Gate_Gold_Or				= 11;
	public static final int Autarchic_Gate_Diamond_And			= 12;
	public static final int Autarchic_Gate_Diamond_Or			= 13;

	public static final int MAX									= 14;

	private int series;

	@SideOnly(Side.CLIENT)
	private Icon[] icons;

	public ItemGate(int i, int series) {
		super(i);

		this.series = series;

		setHasSubtypes(true);
		setMaxDamage(0);
		setPassSneakClick(true);
	}

	@SuppressWarnings({ "all" })
	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIconFromDamage(int i) {
		if (series == 0){	//Normal Gates
			switch (i) {
			case 0:
				return icons[ItemGate.Gate];
			case 1:
				return icons[ItemGate.Gate_Iron_And];
			case 2:
				return icons[ItemGate.Gate_Iron_Or];
			case 3:
				return icons[ItemGate.Gate_Gold_And];
			case 4:
				return icons[ItemGate.Gate_Gold_Or];
			case 5:
				return icons[ItemGate.Gate_Diamond_And];
			default:
				return icons[ItemGate.Gate_Diamond_Or];
			}
		} else if (series == 1){
			switch (i) {
			case 0:
				return icons[ItemGate.Autarchic_Gate];
			case 1:
				return icons[ItemGate.Autarchic_Gate_Iron_And];
			case 2:
				return icons[ItemGate.Autarchic_Gate_Iron_Or];
			case 3:
				return icons[ItemGate.Autarchic_Gate_Gold_And];
			case 4:
				return icons[ItemGate.Autarchic_Gate_Gold_Or];
			case 5:
				return icons[ItemGate.Autarchic_Gate_Diamond_And];
			default:
				return icons[ItemGate.Autarchic_Gate_Diamond_Or];
			}
		}
		return null;
	}

	@Override
	public String getUnlocalizedName(ItemStack itemstack) {
		return (new StringBuilder()).append(super.getUnlocalizedName()).append(".").append(itemstack.getItemDamage()).toString();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List itemList) {
		itemList.add(new ItemStack(this, 1, 0));
		itemList.add(new ItemStack(this, 1, 1));
		itemList.add(new ItemStack(this, 1, 2));
		itemList.add(new ItemStack(this, 1, 3));
		itemList.add(new ItemStack(this, 1, 4));
		itemList.add(new ItemStack(this, 1, 5));
		itemList.add(new ItemStack(this, 1, 6));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconRegister)
	{

		for (IAction action : ActionManager.actions.values()){
			action.registerIcons(iconRegister);
		}

		for (ITrigger trigger : ActionManager.triggers.values()){
			trigger.registerIcons(iconRegister);
		}

		icons = new Icon[ItemGate.MAX];
		icons[ItemGate.Gate] = iconRegister.registerIcon("buildcraft:gate");
		icons[ItemGate.Gate_Iron_And] = iconRegister.registerIcon("buildcraft:gate_iron_and");
		icons[ItemGate.Gate_Iron_Or] = iconRegister.registerIcon("buildcraft:gate_iron_or");
		icons[ItemGate.Gate_Gold_And] = iconRegister.registerIcon("buildcraft:gate_gold_and");
		icons[ItemGate.Gate_Gold_Or] = iconRegister.registerIcon("buildcraft:gate_gold_or");
		icons[ItemGate.Gate_Diamond_And] = iconRegister.registerIcon("buildcraft:gate_diamond_and");
		icons[ItemGate.Gate_Diamond_Or] = iconRegister.registerIcon("buildcraft:gate_diamond_or");

		icons[ItemGate.Autarchic_Gate] = iconRegister.registerIcon("buildcraft:autarchic_gate");
		icons[ItemGate.Autarchic_Gate_Iron_And] = iconRegister.registerIcon("buildcraft:autarchic_gate_iron_and");
		icons[ItemGate.Autarchic_Gate_Iron_Or] = iconRegister.registerIcon("buildcraft:autarchic_gate_iron_or");
		icons[ItemGate.Autarchic_Gate_Gold_And] = iconRegister.registerIcon("buildcraft:autarchic_gate_gold_and");
		icons[ItemGate.Autarchic_Gate_Gold_Or] = iconRegister.registerIcon("buildcraft:autarchic_gate_gold_or");
		icons[ItemGate.Autarchic_Gate_Diamond_And] = iconRegister.registerIcon("buildcraft:autarchic_gate_diamond_and");
		icons[ItemGate.Autarchic_Gate_Diamond_Or] = iconRegister.registerIcon("buildcraft:autarchic_gate_diamond_or");


	}
}
