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
public abstract class EurekaInformation {

	public abstract String getKey();

	public abstract int getIncrement();

	public abstract int getMaxValue();

	public abstract ItemStack getDisplayStack();

	public abstract String getCategory();

	public abstract  EurekaChapter getGui();
}
