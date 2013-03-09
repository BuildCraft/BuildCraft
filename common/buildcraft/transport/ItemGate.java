package buildcraft.transport;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import buildcraft.BuildCraftTransport;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.ItemBuildCraft;

public class ItemGate extends ItemBuildCraft {

	private int series;

	public ItemGate(int i, int series) {
		super(i);

		this.series = series;

		setHasSubtypes(true);
		setMaxDamage(0);
		setCreativeTab(CreativeTabBuildCraft.tabBuildCraft);
	}

	@SuppressWarnings({ "all" })
	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIconFromDamage(int i) {
		int n = 0;
		if (series > 0) {
			n = 3;
		} else {
			n = 2;
		}
		
		if (series == 0){	//Normal Gates
			switch (i) {
			case 0:
				return BuildCraftTransport.instance.itemIcons[IconItemConstants.Gate];
			case 1:
				return BuildCraftTransport.instance.itemIcons[IconItemConstants.Gate_Iron_And];
			case 2:
				return BuildCraftTransport.instance.itemIcons[IconItemConstants.Gate_Iron_Or];
			case 3:
				return BuildCraftTransport.instance.itemIcons[IconItemConstants.Gate_Gold_And];
			case 4:
				return BuildCraftTransport.instance.itemIcons[IconItemConstants.Gate_Gold_Or];
			case 5:
				return BuildCraftTransport.instance.itemIcons[IconItemConstants.Gate_Diamond_And];
			default:
				return BuildCraftTransport.instance.itemIcons[IconItemConstants.Gate_Diamond_Or];
			}
		} else if (series == 1){
			switch (i) {
			case 0:
				return BuildCraftTransport.instance.itemIcons[IconItemConstants.Autarchic_Gate];
			case 1:
				return BuildCraftTransport.instance.itemIcons[IconItemConstants.Autarchic_Gate_Iron_And];
			case 2:
				return BuildCraftTransport.instance.itemIcons[IconItemConstants.Autarchic_Gate_Iron_Or];
			case 3:
				return BuildCraftTransport.instance.itemIcons[IconItemConstants.Autarchic_Gate_Gold_And];
			case 4:
				return BuildCraftTransport.instance.itemIcons[IconItemConstants.Autarchic_Gate_Gold_Or];
			case 5:
				return BuildCraftTransport.instance.itemIcons[IconItemConstants.Autarchic_Gate_Diamond_And];
			default:
				return BuildCraftTransport.instance.itemIcons[IconItemConstants.Autarchic_Gate_Diamond_Or];
			}
		}
		return null;
	}

	@Override
	public String getUnlocalizedName(ItemStack itemstack) {
		return (new StringBuilder()).append(super.getUnlocalizedName()).append(".").append(itemstack.getItemDamage()).toString();
	}

	@SuppressWarnings("unchecked")
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
}
