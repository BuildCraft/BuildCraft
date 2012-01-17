package net.minecraft.src.buildcraft.transport;

import java.util.ArrayList;

import net.minecraft.src.ItemStack;
import net.minecraft.src.buildcraft.core.ItemBuildCraftTexture;

public class ItemGate extends ItemBuildCraftTexture {

	public ItemGate(int i) {
		super (i);
		
		setHasSubtypes(true);
		setMaxDamage(0);
	}
	
	@Override
	public int getIconFromDamage(int i) {
		switch (i) {
		case 0:
			return 2 * 16 + 6;
		case 1:
			return 2 * 16 + 7;
		case 2:
			return 2 * 16 + 8;
		case 3:
			return 2 * 16 + 9;
		case 4:
			return 2 * 16 + 10;
		case 5:
			return 2 * 16 + 11;
		default:
			return 2 * 16 + 12;		
		}
	}

	@Override
	public String getItemNameIS(ItemStack itemstack) {
		return (new StringBuilder()).append(super.getItemName()).append(".")
				.append(itemstack.getItemDamage()).toString();
	}
	
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void addCreativeItems(ArrayList itemList)
    {    	
    	itemList.add(new ItemStack(this, 1, 0));
    	itemList.add(new ItemStack(this, 1, 1));
    	itemList.add(new ItemStack(this, 1, 2));
    	itemList.add(new ItemStack(this, 1, 3));
    	itemList.add(new ItemStack(this, 1, 4));
    	itemList.add(new ItemStack(this, 1, 5));
    	itemList.add(new ItemStack(this, 1, 6));
    }
}
