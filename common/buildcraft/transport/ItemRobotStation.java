/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport;

import buildcraft.core.ItemBuildCraft;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemRobotStation extends ItemBuildCraft {

	public ItemRobotStation(int i) {
		super(i);
	}

	@Override
	public String getUnlocalizedName(ItemStack itemstack) {
		return "item.PipeRobotStation";
	}

	@Override
	public boolean shouldPassSneakingClickToBlock(World worldObj, int x, int y, int z ) {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister par1IconRegister) {
	    // NOOP
	}

	@Override
    @SideOnly(Side.CLIENT)
    public int getSpriteNumber() {
        return 0;
    }

}
