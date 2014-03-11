/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.filler;

import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.core.Box;
import buildcraft.core.blueprints.BptBuilderTemplate;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public interface IFillerPattern {

	public String getUniqueTag();

	@SideOnly(Side.CLIENT)
	public IIcon getIcon();

	public String getDisplayName();

	public BptBuilderTemplate getBlueprint (Box box, World world, ForgeDirection orientation);
}
