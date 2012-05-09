/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.api;

import net.minecraft.src.TileEntity;

/**
 * This class has to be implemented to create new triggers kinds to BuildCraft 
 * gates. There is an instance per kind, which will get called wherever the
 * trigger can be active.
 */
public abstract class Trigger {
	
	public int id;
	
	/**
	 * Creates a new triggers, and stores it in the trigger list
	 */
	public Trigger (int id) {
		this.id = id;
		BuildCraftAPI.triggers [id] = this;
	}
	
	/**
	 * Return the texture file for this trigger icon
	 */
	public abstract String getTextureFile ();
	
	/**
	 * Return the icon id in the texture file
	 */
	public int getIndexInTexture () {
		return 0;
	}
	
	/**
	 * Return true if this trigger can accept parameters
	 */
	public boolean hasParameter () {
		return false;
	}
	
	/**
	 * Return the trigger description in the UI
	 */
	public String getDescription() {
		return "";
	}
	
	/**
	 * Return true if the tile given in parameter activates the trigger, given
	 * the parameters.
	 */
	public boolean isTriggerActive (TileEntity tile, TriggerParameter parameter) {
		return false;
	}
	
	/**
	 * Create parameters for the trigger. As for now, there is only one kind
	 * of trigger parameter available so this subprogram is final.
	 */
	public final TriggerParameter createParameter () {
		return new TriggerParameter();
	}
}
