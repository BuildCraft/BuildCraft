/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.factory.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import buildcraft.core.lib.block.BlockBuildCraft;
import buildcraft.factory.BuildCraftFactory;
import buildcraft.factory.tile.TileMiningWell;

public class BlockMiningWell extends BlockBuildCraft {
    public BlockMiningWell() {
        super(Material.ground);

        setHardness(5F);
        setResistance(10F);
        setStepSound(soundTypeStone);
    }

    @Override
    public void breakBlock(World world, BlockPos pos, Block block, int meta) {
        super.breakBlock(world, pos, block, meta);
        removePipes(world, pos);
    }

    public void removePipes(World world, BlockPos pos) {
        for (int depth = y - 1; depth > 0; depth--) {
            Block pipe = world.getBlock(x, depth, z);
            if (pipe != BuildCraftFactory.plainPipeBlock) {
                break;
            }
            world.setBlockToAir(x, depth, z);
        }
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileMiningWell();
    }

    @Override
    public int getIconGlowLevel(IBlockAccess access, BlockPos pos) {
        if (renderPass < 2) {
            return -1;
        } else {
            TileMiningWell tile = (TileMiningWell) access.getTileEntity(pos);
            return tile.getIconGlowLevel(renderPass);
        }
    }
}
