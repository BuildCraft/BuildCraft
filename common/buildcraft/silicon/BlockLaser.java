/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.silicon;

import java.util.EnumMap;
import java.util.List;
import com.google.common.collect.Maps;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import buildcraft.core.BCCreativeTab;
import buildcraft.core.lib.block.BlockBuildCraft;
import buildcraft.core.lib.client.render.ICustomHighlight;
import buildcraft.core.lib.utils.MatrixUtils;

public class BlockLaser extends BlockBuildCraft implements ICustomHighlight {
    private static final EnumMap<EnumFacing, AxisAlignedBB[]> boxesMap = Maps.newEnumMap(EnumFacing.class);

    static {
        for (EnumFacing face : EnumFacing.values()) {
            AxisAlignedBB[] array = new AxisAlignedBB[2];
            array[0] = new AxisAlignedBB(0, 0, 0, 1, 0.25, 1);
            array[1] = new AxisAlignedBB(5 / 16.0, 4 / 16.0, 5 / 16.0, 11 / 16.0, 13 / 16.0, 11 / 16.0);
            array = MatrixUtils.multiplyAll(array, MatrixUtils.rotateTowardsFace(EnumFacing.UP, face));
            boxesMap.put(face, array);
        }
    }

    public BlockLaser() {
        super(Material.iron, FACING_6_PROP);
        setHardness(10F);
        setCreativeTab(BCCreativeTab.get("main"));
        setDefaultState(getDefaultState().withProperty(FACING_6_PROP, EnumFacing.UP));
    }

    @Override
    public AxisAlignedBB[] getBoxes(IBlockAccess access, BlockPos pos, IBlockState state) {
        return boxesMap.get(state.getValue(FACING_6_PROP));
    }

    @Override
    public double getExpansion() {
        return 0.0075;
    }

    @Override
    public double getBreathingCoefficent() {
        return 0.725;
    }

    // @Override
    // public MovingObjectPosition collisionRayTrace(World wrd, BlockPos pos, Vec3 origin, Vec3 direction) {
    // AxisAlignedBB[] aabbs = getBoxes(wrd, pos, wrd.getBlockState(pos));
    // MovingObjectPosition closest = null;
    // for (AxisAlignedBB aabb : aabbs) {
    // MovingObjectPosition mop = aabb.offset(pos.getX(), pos.getY(), pos.getZ()).calculateIntercept(origin, direction);
    // if (mop != null) {
    // if (closest != null && mop.hitVec.distanceTo(origin) < closest.hitVec.distanceTo(origin)) {
    // closest = mop;
    // } else {
    // closest = mop;
    // }
    // }
    // }
    // if (closest != null) {
    // closest.hitVec = Utils.convertMiddle(pos);
    //
    // // closest.blockX = x;
    // // closest.blockY = y;
    // // closest.blockZ = z;
    // }
    // return closest;
    // }

    @Override
    @SuppressWarnings("unchecked")
    public void addCollisionBoxesToList(World wrd, BlockPos pos, IBlockState state, AxisAlignedBB mask, List list, Entity ent) {
        AxisAlignedBB[] aabbs = getBoxes(wrd, pos, state);
        for (AxisAlignedBB aabb : aabbs) {
            AxisAlignedBB aabbTmp = aabb.offset(pos.getX(), pos.getY(), pos.getZ());
            if (mask.intersectsWith(aabbTmp)) {
                list.add(aabbTmp);
            }
        }
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean isFullCube() {
        return false;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TileLaser();
    }

    @Override
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta,
            EntityLivingBase placer) {
        return super.onBlockPlaced(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer);
    }

    // @Override
    // public int onBlockPlaced(World world, BlockPos pos, EnumFacing face, float par6, float par7, float par8, int
    // meta) {
    // super.onBlockPlaced(world, pos, side, par6, par7, par8, meta);
    //
    // int retMeta = meta;
    //
    // if (side <= 6) {
    // retMeta = side;
    // }
    //
    // return retMeta;
    // }

    @Override
    public boolean isSideSolid(IBlockAccess world, BlockPos pos, EnumFacing side) {
        return false;
    }
}
