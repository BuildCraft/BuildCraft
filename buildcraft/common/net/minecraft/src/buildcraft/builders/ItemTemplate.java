package net.minecraft.src.buildcraft.builders;

import java.util.Properties;

import net.minecraft.src.Entity;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.StringTranslate;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.core.CoreProxy;

public class ItemTemplate extends Item {

	public ItemTemplate(int i) {
		super(i);
		
		maxStackSize = 1;
		
		iconIndex = ModLoader.addOverride("/gui/items.png",
				"/net/minecraft/src/buildcraft/builders/gui/template.png");
	}

    public String getItemNameIS(ItemStack itemstack) {
    	if (itemstack.getItemDamage() == 0) {
    		return getItemName();
    	} else {    		
    		String id = getItemName() + "#" + itemstack.getItemDamage();
    		
			try {
				Properties properties = (Properties) ModLoader.getPrivateValue(
						net.minecraft.src.StringTranslate.class,
						StringTranslate.getInstance(), 1);
				
				String s1 = (new StringBuilder(id)).append(".name").toString();
				
				if (properties.get(s1) == null) {
					CoreProxy.addLocalization(s1,
							"Template #" + itemstack.getItemDamage());
				}
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
			return id;
    	}
    }
    
    public void onUpdate(ItemStack itemstack, World world, Entity entity, int i, boolean flag) {
    }
    
}
