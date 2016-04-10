/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import buildcraft.BuildCraftRobotics;
import buildcraft.core.GuiIds;
import buildcraft.core.lib.block.BlockBuildCraft;

public class BlockZonePlan extends BlockBuildCraft {
    public BlockZonePlan() {
        super(Material.iron, FACING_PROP);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileZonePlan();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entityplayer, EnumFacing side, float hitX, float hitY,
            float hitZ) {
        if (super.onBlockActivated(world, pos, state, entityplayer, side, hitX, hitY, hitZ)) {
            return true;
        }

        if (!world.isRemote) {
            entityplayer.openGui(BuildCraftRobotics.instance, GuiIds.MAP, world, pos.getX(), pos.getY(), pos.getZ());
        }

        return true;
    }
}
