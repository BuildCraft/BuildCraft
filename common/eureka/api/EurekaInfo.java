package eureka.api;

import eureka.api.client.gui.EurekaChapter;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;

/**
 * Copyright (c) 2014, AEnterprise
 * http://buildcraftadditions.wordpress.com/
 * Buildcraft Additions is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://buildcraftadditions.wordpress.com/wiki/licensing-stuff/
 */
public class EurekaInfo extends EurekaInformation {
	public String key, category;
	public int increment, maxValue;
	public ItemStack stack;
	public EurekaChapter gui;
	public ArrayList<String> requiredResearch;

	public EurekaInfo(String key, String category, int increment, int maxValue, ItemStack stack, EurekaChapter gui, ArrayList<String> requiredResearch){
		this.key = key;
		this.category = category;
		this.increment = increment;
		this.maxValue = maxValue;
		this.stack = stack;
		this.gui = gui;
		this.requiredResearch = requiredResearch;
	}

	public EurekaInfo(String key, String category, int increment, int maxValue, ItemStack stack, EurekaChapter gui){
		this.key = key;
		this.category = category;
		this.increment = increment;
		this.maxValue = maxValue;
		this.stack = stack;
		this.gui = gui;
		this.requiredResearch =new  ArrayList<String>();
	}

	@Override
	public String getKey() {
		return key;
	}

	@Override
	public int getIncrement() {
		return increment;
	}

	@Override
	public int getMaxValue() {
		return maxValue;
	}

	@Override
	public ItemStack getDisplayStack() {
		return stack;
	}

	@Override
	public String getCategory() {
		return category;
	}

	@Override
	public EurekaChapter getGui() {
		return gui;
	}

	@Override
	public ArrayList<String> getRequiredResearch() {
		return requiredResearch;
	}
}
