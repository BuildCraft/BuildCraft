/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.factory.block;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;

import buildcraft.core.CoreConstants;
import buildcraft.core.lib.block.BlockBuildCraftBase;

public class BlockPlainPipe extends BlockBuildCraftBase {

    public BlockPlainPipe() {
        super(Material.glass);

        minX = CoreConstants.PIPE_MIN_POS;
        minY = 0.0;
        minZ = CoreConstants.PIPE_MIN_POS;

        maxX = CoreConstants.PIPE_MAX_POS;
        maxY = 1.0;
        maxZ = CoreConstants.PIPE_MAX_POS;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean isNormalCube() {
        return false;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        list.add(new ItemStack(this));
    }

    @Override
    public Item getItemDropped(IBlockState state, Random random, int j) {
        return null;
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        return Collections.emptyList();
    }

    @Override
    public boolean isLadder(IBlockAccess world, BlockPos pos, EntityLivingBase entity) {
        return true;
    }
}
