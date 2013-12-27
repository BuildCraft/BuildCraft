/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.triggers;

import buildcraft.api.gates.ActionManager;
import static buildcraft.api.gates.ActionManager.actions;
import buildcraft.api.gates.IAction;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.util.Icon;

public abstract class BCAction implements IAction {

	protected final String uniqueTag;

	/**
	 * UniqueTag accepts multiple possible tags, use this feature to migrate to
	 * more standardized tags if needed, otherwise just pass a single string.
	 * The first passed string will be the one used when saved to disk.
	 *
	 * @param uniqueTag
	 */
	public BCAction(String... uniqueTag) {
		this.uniqueTag = uniqueTag[0];
		for (String tag : uniqueTag) {
			ActionManager.actions.put(tag, this);
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
	public Icon getIcon() {
		return ActionTriggerIconProvider.INSTANCE.getIcon(getIconIndex());
	}

	public int getTextureMap() {
		return 1;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconRegister) {
	}

	@Override
	public boolean hasParameter() {
		return false;
	}
}
