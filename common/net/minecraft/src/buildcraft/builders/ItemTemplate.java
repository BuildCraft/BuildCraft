/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.builders;

import java.util.Properties;

import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.Entity;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.StringTranslate;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.forge.ITextureProvider;

public class ItemTemplate extends Item implements ITextureProvider {

	public ItemTemplate(int i) {
		super(i);
		
		maxStackSize = 1;
		
		iconIndex = 2 * 16 + 0;
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

	@Override
	public String getTextureFile() {
		return BuildCraftCore.customBuildCraftSprites;
	}
    
}
