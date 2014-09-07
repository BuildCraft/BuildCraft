package eureka.api;

import eureka.api.client.gui.EurekaChapter;
import net.minecraft.item.ItemStack;

/**
 * Copyright (c) 2014, AEnterprise
 * http://buildcraftadditions.wordpress.com/
 * Buildcraft Additions is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://buildcraftadditions.wordpress.com/wiki/licensing-stuff/
 */
public class EurekaEntry{
	public String category;
	public int increment, maxValue;
	public ItemStack stack;
	public Class<? extends EurekaChapter> chapterClass;

	public EurekaEntry(String category, int increment, int maxValue, ItemStack stack, Class <? extends EurekaChapter> chapterClass){
		this.category = category;
		this.increment = increment;
		this.maxValue = maxValue;
		this.stack = stack;
		this.chapterClass = chapterClass;
	}
}
