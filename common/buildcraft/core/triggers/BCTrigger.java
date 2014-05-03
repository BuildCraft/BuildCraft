/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.triggers;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import buildcraft.api.gates.ActionManager;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.api.gates.TriggerParameter;

/**
 * This class has to be implemented to create new triggers kinds to BuildCraft
 * gates. There is an instance per kind, which will get called wherever the
 * trigger can be active.
 */
public abstract class BCTrigger implements ITrigger {

	protected final String uniqueTag;

	/**
	 * UniqueTag accepts multiple possible tags, use this feature to migrate to
	 * more standardized tags if needed, otherwise just pass a single string.
	 * The first passed string will be the one used when saved to disk.
	 *
	 * @param uniqueTag
	 */
	public BCTrigger(String... uniqueTag) {
		this.uniqueTag = uniqueTag[0];
		for (String tag : uniqueTag) {
			ActionManager.triggers.put(tag, this);
		}
	}

	@Override
	public String getUniqueTag() {
		return uniqueTag;
	}

	public int getIconIndex() {
		return 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon() {
		return ActionTriggerIconProvider.INSTANCE.getIcon(getIconIndex());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister iconRegister) {
	}

	@Override
	public boolean hasParameter() {
		return false;
	}

	@Override
	public boolean requiresParameter() {
		return false;
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public final ITriggerParameter createParameter() {
		return new TriggerParameter();
	}
}
