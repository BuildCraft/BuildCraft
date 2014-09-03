package buildcraft.research;

import eureka.api.client.gui.EurekaChapter;

/**
 * Copyright (c) 2014, AEnterprise
 * http://buildcraftadditions.wordpress.com/
 * Buildcraft Additions is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://buildcraftadditions.wordpress.com/wiki/licensing-stuff/
 */
public class quarryEurekaGui extends EurekaChapter {
	@Override
	public String getText(int page) {
		return "makes large holes in the ground";
	}

	@Override
	public void drawCustomStuff(int page) {

	}

	@Override
	public boolean hasNextPage(int page) {
		return false;
	}

	@Override
	public String getRequiredResearch() {
		return "none at the moment";
	}

	@Override
	public String howToMakeProgress() {
		return "place mining wells";
	}
}
