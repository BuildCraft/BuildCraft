/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.api.gates;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;

/**
 * This class has to be implemented to create new triggers kinds to BuildCraft gates. There is an instance per kind, which will get called wherever the trigger
 * can be active.
 */
public abstract class Trigger implements ITrigger {

	protected int id;

	/**
	 * Creates a new triggers, and stores it in the trigger list
	 */
	public Trigger(int id) {
		this.id = id;
		ActionManager.triggers[id] = this;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see net.minecraft.src.buildcraft.api.gates.ITrigger#getId()
	 */
	@Override
	public int getId() {
		return this.id;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see net.minecraft.src.buildcraft.api.gates.ITrigger#getTextureFile()
	 */
    @SideOnly(Side.CLIENT)
	@Override
	public abstract Icon getTextureIcon();

	/*
	 * (non-Javadoc)
	 *
	 * @see net.minecraft.src.buildcraft.api.gates.ITrigger#hasParameter()
	 */
	@Override
	public boolean hasParameter() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see net.minecraft.src.buildcraft.api.gates.ITrigger#getDescription()
	 */
	@Override
	public String getDescription() {
		return "";
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see net.minecraft.src.buildcraft.api.gates.ITrigger#isTriggerActive(net.minecraft.src.TileEntity,
	 * net.minecraft.src.buildcraft.api.gates.TriggerParameter)
	 */
	@Override
	public boolean isTriggerActive(TileEntity tile, ITriggerParameter parameter) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see net.minecraft.src.buildcraft.api.gates.ITrigger#createParameter()
	 */
	@Override
	public final ITriggerParameter createParameter() {
		return new TriggerParameter();
	}
}
