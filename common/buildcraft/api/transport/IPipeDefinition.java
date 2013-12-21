/*
 * Copyright (c) SpaceToad, 2011-2012
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.transport;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public interface IPipeDefinition {

	String getUniqueTag();

	void registerIcons(IconRegister iconRegister);

	Icon getIcon(int index);

	Icon getItemIcon();

	PipeBehavior makePipeBehavior(TileEntity tile);
}
