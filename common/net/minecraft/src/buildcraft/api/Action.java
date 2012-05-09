/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.api;

public abstract class Action {	
	public int id;
	
	public Action (int id) {
		this.id = id;
		BuildCraftAPI.actions [id] = this;
	}
	
	public abstract String getTexture ();
	
	public int getIndexInTexture () {
		return 0;
	}
	
	public boolean hasParameter () {
		return false;
	}

	public String getDescription() {
		return "";
	}
}
