package buildcraft.core;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import buildcraft.BuildCraftCore;

public class CreativeTabBuildCraft extends CreativeTabs {

	public static final CreativeTabs tabBuildCraft = new CreativeTabBuildCraft("buildcraft");
	
	public CreativeTabBuildCraft(String label) {
		super(label);
	}

	@Override
    public ItemStack getIconItemStack() {
		return new ItemStack(BuildCraftCore.diamondGearItem);
	}
	
	@Override
    public String getTranslatedTabLabel() {
		return "BuildCraft";
	}
}
