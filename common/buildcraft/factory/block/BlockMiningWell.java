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
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        super.breakBlock(world, pos, state);
        removePipes(world, pos);
    }

    public void removePipes(World world, BlockPos pos) {
        for (int y = 1; y < pos.getY(); y++) {
            BlockPos down = pos.down(y);
            Block block = world.getBlockState(down).getBlock();
            if (block != BuildCraftFactory.plainPipeBlock) {
                break;
            }
            world.setBlockToAir(down);
        }
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileMiningWell();
    }
}
