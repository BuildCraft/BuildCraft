package eureka.api;

import eureka.api.client.gui.EurekaChapter;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Copyright (c) 2014, AEnterprise
 * http://buildcraftAdditions.wordpress.com/
 * Eureka is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://buildcraftAdditions.wordpress.com/wiki/licensing-stuff/
 */
public class EurekaRegistry {
    private static HashMap<String, EurekaInformation> chapters = new HashMap<String, EurekaInformation>(50);
	private static ArrayList<String> keys = new ArrayList<String>(50);
	private static ArrayList<String> categoriesList = new ArrayList<String>(20);
	private static HashMap<String, ItemStack> categories = new HashMap<String, ItemStack>(20);

    /**
     * Register your keys here for the EUREKA system
     */
    public static void registerKey(EurekaInformation information){
		    chapters.put(information.getKey(), information);
		    keys.add(information.getKey());
	    }

	public static void registerCategory(String category, ItemStack stack){
		categoriesList.add(category);
		categories.put(category, stack);
	}

    /**
     * @return a clone of the list containing all EUREKA keys
     */
    public static ArrayList<String> getKeys(){
        return (ArrayList) keys.clone();
    }

    public static int getMaxValue(String key){
        if (!keys.contains(key))
            return 0;
        return chapters.get(key).getMaxValue();
    }

    public static int getIncrement(String key){
        if (!keys.contains(key))
            return 0;
        return chapters.get(key).getIncrement();
    }

	public static ItemStack getDisplayStack(String key){
		if (!keys.contains(key))
			return null;
		return chapters.get(key).getDisplayStack();
	}

	public static EurekaChapter getChapterGui(String key){
		return chapters.get(key).getGui();
	}

	public static String getCategory(String key){
		return chapters.get(key).getCategory();
	}

	public static ArrayList<String> getCategoriesList(){
		return (ArrayList) categoriesList.clone();
	}

	public static ItemStack getCategoryDisplayStack(String category){
		return categories.get(category);
	}


}
