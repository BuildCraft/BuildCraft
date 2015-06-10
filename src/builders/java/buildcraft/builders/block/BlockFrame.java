/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import buildcraft.core.BuildCraftCore;
import buildcraft.core.CoreConstants;
import buildcraft.core.internal.IFramePipeConnection;
import buildcraft.core.lib.utils.Utils;

public class BlockFrame extends Block implements IFramePipeConnection {

    public BlockFrame() {
        super(Material.glass);
        setHardness(0.5F);
    }

    @Override
    public void breakBlock(World world, BlockPos pos, Block block, int meta) {
        if (world.isRemote) {
            return;
        }

        removeNeighboringFrames(world, pos);
    }

    public void removeNeighboringFrames(World world, BlockPos pos) {
        for (EnumFacing dir : EnumFacing.VALID_DIRECTIONS) {
            Block nBlock = world.getBlock(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ);
            if (nBlock == this) {
                world.setBlockToAir(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ);
            }
        }
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public Item getItemDropped(int i, Random random, int j) {
        return null;
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, BlockPos pos, int metadata, int fortune) {
        return new ArrayList<ItemStack>();
    }

    @Override
    public int getRenderType() {
        return BuildCraftCore.legacyPipeModel;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, BlockPos pos) {
        float xMin = CoreConstants.PIPE_MIN_POS, xMax = CoreConstants.PIPE_MAX_POS, yMin = CoreConstants.PIPE_MIN_POS, yMax =
            CoreConstants.PIPE_MAX_POS, zMin = CoreConstants.PIPE_MIN_POS, zMax = CoreConstants.PIPE_MAX_POS;

        if (Utils.checkLegacyPipesConnections(world, pos, i - 1, j, k)) {
            xMin = 0.0F;
        }

        if (Utils.checkLegacyPipesConnections(world, pos, i + 1, j, k)) {
            xMax = 1.0F;
        }

        if (Utils.checkLegacyPipesConnections(world, pos, i, j - 1, k)) {
            yMin = 0.0F;
        }

        if (Utils.checkLegacyPipesConnections(world, pos, i, j + 1, k)) {
            yMax = 1.0F;
        }

        if (Utils.checkLegacyPipesConnections(world, pos, pos - 1)) {
            zMin = 0.0F;
        }

        if (Utils.checkLegacyPipesConnections(world, pos, pos + 1)) {
            zMax = 1.0F;
        }

        return AxisAlignedBB.getBoundingBox((double) i + xMin, (double) j + yMin, (double) k + zMin, (double) i + xMax, (double) j + yMax, (double) k
            + zMax);
    }

    @Override
    @SuppressWarnings({ "all" })
    // @Override (client only)
            public
            AxisAlignedBB getSelectedBoundingBoxFromPool(World world, BlockPos pos) {
        return getCollisionBoundingBoxFromPool(world, pos);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void addCollisionBoxesToList(World world, BlockPos pos, AxisAlignedBB axisalignedbb, List arraylist, Entity par7Entity) {
        setBlockBounds(CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MAX_POS,
            CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS);
        super.addCollisionBoxesToList(world, pos, axisalignedbb, arraylist, par7Entity);

        if (Utils.checkLegacyPipesConnections(world, pos, i - 1, j, k)) {
            setBlockBounds(0.0F, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS,
                CoreConstants.PIPE_MAX_POS);
            super.addCollisionBoxesToList(world, pos, axisalignedbb, arraylist, par7Entity);
        }

        if (Utils.checkLegacyPipesConnections(world, pos, i + 1, j, k)) {
            setBlockBounds(CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, 1.0F, CoreConstants.PIPE_MAX_POS,
                CoreConstants.PIPE_MAX_POS);
            super.addCollisionBoxesToList(world, pos, axisalignedbb, arraylist, par7Entity);
        }

        if (Utils.checkLegacyPipesConnections(world, pos, i, j - 1, k)) {
            setBlockBounds(CoreConstants.PIPE_MIN_POS, 0.0F, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS,
                CoreConstants.PIPE_MAX_POS);
            super.addCollisionBoxesToList(world, pos, axisalignedbb, arraylist, par7Entity);
        }

        if (Utils.checkLegacyPipesConnections(world, pos, i, j + 1, k)) {
            setBlockBounds(CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MAX_POS, 1.0F,
                CoreConstants.PIPE_MAX_POS);
            super.addCollisionBoxesToList(world, pos, axisalignedbb, arraylist, par7Entity);
        }

        if (Utils.checkLegacyPipesConnections(world, pos, pos - 1)) {
            setBlockBounds(CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, 0.0F, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS,
                CoreConstants.PIPE_MAX_POS);
            super.addCollisionBoxesToList(world, pos, axisalignedbb, arraylist, par7Entity);
        }

        if (Utils.checkLegacyPipesConnections(world, pos, pos + 1)) {
            setBlockBounds(CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MAX_POS,
                CoreConstants.PIPE_MAX_POS, 1.0F);
            super.addCollisionBoxesToList(world, pos, axisalignedbb, arraylist, par7Entity);
        }

        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public MovingObjectPosition collisionRayTrace(World world, BlockPos pos, Vec3 vec3d, Vec3 vec3d1) {
        float xMin = CoreConstants.PIPE_MIN_POS, xMax = CoreConstants.PIPE_MAX_POS, yMin = CoreConstants.PIPE_MIN_POS, yMax =
            CoreConstants.PIPE_MAX_POS, zMin = CoreConstants.PIPE_MIN_POS, zMax = CoreConstants.PIPE_MAX_POS;

        if (Utils.checkLegacyPipesConnections(world, pos, i - 1, j, k)) {
            xMin = 0.0F;
        }

        if (Utils.checkLegacyPipesConnections(world, pos, i + 1, j, k)) {
            xMax = 1.0F;
        }

        if (Utils.checkLegacyPipesConnections(world, pos, i, j - 1, k)) {
            yMin = 0.0F;
        }

        if (Utils.checkLegacyPipesConnections(world, pos, i, j + 1, k)) {
            yMax = 1.0F;
        }

        if (Utils.checkLegacyPipesConnections(world, pos, pos - 1)) {
            zMin = 0.0F;
        }

        if (Utils.checkLegacyPipesConnections(world, pos, pos + 1)) {
            zMax = 1.0F;
        }

        setBlockBounds(xMin, yMin, zMin, xMax, yMax, zMax);

        MovingObjectPosition r = super.collisionRayTrace(world, pos, vec3d, vec3d1);

        setBlockBounds(0, 0, 0, 1, 1, 1);

        return r;
    }

    @Override
    public boolean isPipeConnected(IBlockAccess blockAccess, BlockPos one, BlockPos two) {
        return blockAccess.getBlockState(one).getBlock() == this;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        list.add(new ItemStack(this));
    }

//    @Override
//    public void registerBlockIcons(TextureAtlasSpriteRegister register) {
//        blockIcon = register.registerIcon("buildcraftbuilders:frameBlock/default");
//    }
}
