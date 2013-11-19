/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.triggers;

import buildcraft.api.gates.ActionManager;
import buildcraft.api.gates.IAction;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.util.Icon;

public abstract class BCAction implements IAction {

	protected final int legacyId;
	protected final String uniqueTag;

	public BCAction(int legacyId, String uniqueTag) {
		this.legacyId = legacyId;
		this.uniqueTag = uniqueTag;
		ActionManager.registerAction(this);
	}

	@Override
	public String getUniqueTag() {
		return uniqueTag;
	}

	@Override
	public int getLegacyId() {
		return this.legacyId;
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
