package buildcraft.research;

import eureka.api.client.gui.EurekaChapter;

/**
 * Copyright (c) 2014, AEnterprise
 * http://buildcraftadditions.wordpress.com/
 * Buildcraft Additions is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://buildcraftadditions.wordpress.com/wiki/licensing-stuff/
 */
public class BasicEurekaChapter extends EurekaChapter{
	public String key;
	public boolean needsResearch;

	public BasicEurekaChapter(String key, boolean needsResearch){
		this.key = key;
		this.needsResearch = needsResearch;
	}

	@Override
	public String getText(int page) {
		return Utils.localize("engineeringDiary." + key + ".page" + page);
	}

	@Override
	public void drawCustomStuff(int page) {

	}

	@Override
	public boolean hasNextPage(int page) {
		return !(Utils.localize("engineeringDiary." + key + ".page" + (page + 1)).equals("engineeringDiary." + key + ".page" + Integer.toString(page + 1)));
	}

	@Override
	public String getRequiredResearch() {
		if (needsResearch)
			return Utils.localize("engineeringDiary." + key + ".requiredResearch");
		return Utils.localize("engineeringDiary.noRequiredResearch");
	}

	@Override
	public String howToMakeProgress() {
		return Utils.localize("engineeringDiary." + key + "howToMakeProgress");
	}
}
