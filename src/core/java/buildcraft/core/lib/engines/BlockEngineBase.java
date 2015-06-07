/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.engines;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

import buildcraft.api.events.BlockInteractionEvent;
import buildcraft.api.transport.IItemPipe;
import buildcraft.core.lib.block.BlockBuildCraft;
import buildcraft.core.lib.render.ICustomHighlight;

public abstract class BlockEngineBase extends BlockBuildCraft implements ICustomHighlight {
    private static final AxisAlignedBB[][] boxes = {
        { new AxisAlignedBB(0.0, 0.5, 0.0, 1.0, 1.0, 1.0), new AxisAlignedBB(0.25, 0.0, 0.25, 0.75, 0.5, 0.75) }, // -Y
        { new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.5, 1.0), new AxisAlignedBB(0.25, 0.5, 0.25, 0.75, 1.0, 0.75) }, // +Y
        { new AxisAlignedBB(0.0, 0.0, 0.5, 1.0, 1.0, 1.0), new AxisAlignedBB(0.25, 0.25, 0.0, 0.75, 0.75, 0.5) }, // -Z
        { new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 0.5), new AxisAlignedBB(0.25, 0.25, 0.5, 0.75, 0.75, 1.0) }, // +Z
        { new AxisAlignedBB(0.5, 0.0, 0.0, 1.0, 1.0, 1.0), new AxisAlignedBB(0.0, 0.25, 0.25, 0.5, 0.75, 0.75) }, // -X
        { new AxisAlignedBB(0.0, 0.0, 0.0, 0.5, 1.0, 1.0), new AxisAlignedBB(0.5, 0.25, 0.25, 1.0, 0.75, 0.75) } // +X
        };

    public BlockEngineBase() {
        super(Material.iron, new IProperty[] { ENGINE_TYPE });
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public int getRenderType() {
        return 3;
    }

    @Override
    public boolean isSideSolid(IBlockAccess world, BlockPos pos, EnumFacing side) {
        TileEntity tile = world.getTileEntity(pos);

        if (tile instanceof TileEngineBase) {
            return ((TileEngineBase) tile).orientation.getOpposite() == side;
        } else {
            return false;
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float par7, float par8,
            float par9) {
        TileEntity tile = world.getTileEntity(pos);

        BlockInteractionEvent event = new BlockInteractionEvent(player, state);
        FMLCommonHandler.instance().bus().post(event);
        if (event.isCanceled()) {
            return false;
        }

        // Do not open guis when having a pipe in hand
        if (player.getCurrentEquippedItem() != null) {
            if (player.getCurrentEquippedItem().getItem() instanceof IItemPipe) {
                return false;
            }
        }

        if (tile instanceof TileEngineBase) {
            return ((TileEngineBase) tile).onBlockActivated(player, side);
        }

        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void addCollisionBoxesToList(World wrd, BlockPos pos, IBlockState state, AxisAlignedBB mask, List list, Entity ent) {
        TileEntity tile = wrd.getTileEntity(pos);
        if (tile instanceof TileEngineBase) {
            AxisAlignedBB[] aabbs = boxes[((TileEngineBase) tile).orientation.ordinal()];
            for (AxisAlignedBB aabb : aabbs) {
                AxisAlignedBB aabbTmp = aabb.offset(pos.getX(), pos.getY(), pos.getZ());
                if (mask.intersectsWith(aabbTmp)) {
                    list.add(aabbTmp);
                }
            }
        } else {
            super.addCollisionBoxesToList(wrd, pos, state, mask, list, ent);
        }
    }

    @Override
    public AxisAlignedBB[] getBoxes(World wrd, BlockPos pos, EntityPlayer player) {
        TileEntity tile = wrd.getTileEntity(pos);
        if (tile instanceof TileEngineBase) {
            return boxes[((TileEngineBase) tile).orientation.ordinal()];
        } else {
            return new AxisAlignedBB[] { new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0) };
        }
    }

    @Override
    public double getExpansion() {
        return 0.0075;
    }

    @Override
    public MovingObjectPosition collisionRayTrace(World wrd, BlockPos pos, Vec3 origin, Vec3 direction) {
        TileEntity tile = wrd.getTileEntity(pos);
        if (tile instanceof TileEngineBase) {
            AxisAlignedBB[] aabbs = boxes[((TileEngineBase) tile).orientation.ordinal()];
            MovingObjectPosition closest = null;
            for (AxisAlignedBB aabb : aabbs) {
                MovingObjectPosition mop = aabb.offset(pos.getX(), pos.getY(), pos.getZ()).calculateIntercept(origin, direction);
                if (mop != null) {
                    if (closest != null && mop.hitVec.distanceTo(origin) < closest.hitVec.distanceTo(origin)) {
                        closest = mop;
                    } else {
                        closest = mop;
                    }
                }
            }
            return closest;
        } else {
            return super.collisionRayTrace(wrd, pos, origin, direction);
        }
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, entity, stack);
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEngineBase) {
            TileEngineBase engine = (TileEngineBase) tile;
            engine.orientation = EnumFacing.UP;
            if (!engine.isOrientationValid()) {
                engine.switchOrientation(true);
            }
        }
    }

    @Override
    public int damageDropped(IBlockState state) {
        return ENGINE_TYPE.getValue(state).ordinal();
    }

    @SuppressWarnings({ "all" })
    @Override
    public void randomDisplayTick(World world, BlockPos pos, IBlockState state, Random random) {
        TileEntity tile = world.getTileEntity(pos);

        if (!(tile instanceof TileEngineBase)) {
            return;
        }
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        if (((TileEngineBase) tile).getEnergyStage() == TileEngineBase.EnergyStage.OVERHEAT) {
            for (int f = 0; f < 16; f++) {
                world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x + 0.4F + (random.nextFloat() * 0.2F), y + (random.nextFloat() * 0.5F), z + 0.4F
                    + (random.nextFloat() * 0.2F), random.nextFloat() * 0.04F - 0.02F, random.nextFloat() * 0.05F + 0.02F,
                    random.nextFloat() * 0.04F - 0.02F);
            }
        } else if (((TileEngineBase) tile).isBurning()) {
            float f = x + 0.5F;
            float f1 = y + 0.0F + (random.nextFloat() * 6F) / 16F;
            float f2 = z + 0.5F;
            float f3 = 0.52F;
            float f4 = random.nextFloat() * 0.6F - 0.3F;
            world.spawnParticle(EnumParticleTypes.REDSTONE, f - f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
            world.spawnParticle(EnumParticleTypes.REDSTONE, f + f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
            world.spawnParticle(EnumParticleTypes.REDSTONE, f + f4, f1, f2 - f3, 0.0D, 0.0D, 0.0D);
            world.spawnParticle(EnumParticleTypes.REDSTONE, f + f4, f1, f2 + f3, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighbour) {
        TileEntity tile = world.getTileEntity(pos);

        if (tile instanceof TileEngineBase) {
            ((TileEngineBase) tile).onNeighborUpdate();
        }
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return null;
    }

    public abstract String getUnlocalizedName(int metadata);

    public abstract TileEntity createTileEntity(World world, int metadata);
}
