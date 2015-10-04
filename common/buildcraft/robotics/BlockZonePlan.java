/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import buildcraft.BuildCraftRobotics;
import buildcraft.core.GuiIds;
import buildcraft.core.lib.block.BlockBuildCraft;
import buildcraft.robotics.map.MapWorld;

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

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, entity, stack);

        if (!world.isRemote) {
            int radius = TileZonePlan.RESOLUTION >> 4;

            int chunkX = pos.getX() >> 4;
            int chunkZ = pos.getZ() >> 4;

            MapWorld map = BuildCraftRobotics.manager.getWorld(world);

            for (int checkX = -radius; checkX < radius; checkX++) {
                for (int checkZ = -radius; checkZ < radius; checkZ++) {
                    int distance = checkX * checkX + checkZ * checkZ;
                    map.queueChunkForUpdateIfEmpty(chunkX + checkX, chunkZ + checkZ, distance);
                }
            }
        }
    }
}
