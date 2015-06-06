/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.items;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import buildcraft.core.BCCreativeTab;

public class ItemBuildCraft extends Item {
    private boolean passSneakClick = false;

    public ItemBuildCraft() {
        this(BCCreativeTab.get("main"));
    }

    public ItemBuildCraft(CreativeTabs creativeTab) {
        super();

        setCreativeTab(creativeTab);
    }

    public Item setPassSneakClick(boolean passClick) {
        this.passSneakClick = passClick;
        return this;
    }

    @Override
    public boolean doesSneakBypassUse(World world, BlockPos pos, EntityPlayer player) {
        return passSneakClick;
    }
}
