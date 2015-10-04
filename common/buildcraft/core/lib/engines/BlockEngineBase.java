/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.engines;

import java.util.Map;
import java.util.Random;

import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

import buildcraft.api.enums.EnumEnergyStage;
import buildcraft.api.events.BlockInteractionEvent;
import buildcraft.api.transport.IItemPipe;
import buildcraft.core.lib.block.BlockBuildCraft;
import buildcraft.core.lib.render.ICustomHighlight;
import buildcraft.core.lib.utils.Utils;

public abstract class BlockEngineBase extends BlockBuildCraft implements ICustomHighlight {
    private static final Map<EnumFacing, AxisAlignedBB[]> boxMap;

    static {
        Map<EnumFacing, AxisAlignedBB[]> map = Maps.newEnumMap(EnumFacing.class);
        for (EnumFacing face : EnumFacing.values()) {
            AxisAlignedBB[] array = new AxisAlignedBB[2];
            boolean pos = face.getAxisDirection() == AxisDirection.POSITIVE;

            Vec3 pointA = Utils.withValue(Utils.VEC_ZERO, face.getAxis(), pos ? 0 : 0.5);
            Vec3 pointB = Utils.withValue(Utils.VEC_ONE, face.getAxis(), pos ? 0.5 : 1);
            array[0] = Utils.boundingBox(pointA, pointB);

            pointA = Utils.vec3(0.25).add(Utils.convert(face, 0.25));
            pointB = pointA.add(Utils.VEC_HALF);
            array[1] = Utils.boundingBox(pointA, pointB);
            map.put(face, array);
        }
        boxMap = Maps.immutableEnumMap(map);
    }

    public BlockEngineBase() {
        super(Material.iron, ENGINE_TYPE);
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
    public AxisAlignedBB[] getBoxes(IBlockAccess world, BlockPos pos, IBlockState state) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEngineBase) {
            return boxMap.get(((TileEngineBase) tile).orientation);
        } else {
            return super.getBoxes(world, pos, state);
        }
    }

    @Override
    public double getExpansion() {
        return 0.0075;
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

        if (((TileEngineBase) tile).getEnergyStage() == EnumEnergyStage.OVERHEAT) {
            for (int f = 0; f < 16; f++) {
                world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x + 0.4F + (random.nextFloat() * 0.2F), y + (random.nextFloat() * 0.5F), z + 0.4F
                    + (random.nextFloat() * 0.2F), random.nextFloat() * 0.04F - 0.02F, random.nextFloat() * 0.05F + 0.02F, random.nextFloat() * 0.04F
                        - 0.02F);
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
        return createTileEntity(world, metadata);
    }

    public abstract String getUnlocalizedName(int metadata);

    public abstract TileEntity createTileEntity(World world, int metadata);
}
