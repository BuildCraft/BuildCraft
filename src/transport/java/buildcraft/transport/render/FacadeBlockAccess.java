package buildcraft.transport.render;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.BiomeGenBase;

import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.pluggable.IFacadePluggable;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.core.CompatHooks;

public class FacadeBlockAccess implements IBlockAccess {
    private final IBlockAccess world;
    private final EnumFacing side;

    public FacadeBlockAccess(IBlockAccess world, EnumFacing side) {
        this.world = world;
        this.side = side;
    }

    @Override
    public Block getBlock(BlockPos pos) {
        Block compatBlock = CompatHooks.INSTANCE.getVisualBlock(world, pos, side);
        if (compatBlock != null) {
            return compatBlock;
        }

        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof IPipeTile) {
            PipePluggable p = ((IPipeTile) tile).getPipePluggable(side);
            if (p instanceof IFacadePluggable) {
                return ((IFacadePluggable) p).getCurrentBlock();
            }
        }
        return Blocks.air;
    }

    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        return null;
    }

    @Override
    public int getLightBrightnessForSkyBlocks(BlockPos pos, int a) {
        return 0;
    }

    @Override
    public int getBlockMetadata(BlockPos pos) {
        int compatMeta = CompatHooks.INSTANCE.getVisualMeta(world, pos, side);
        if (compatMeta >= 0) {
            return compatMeta;
        }

        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof IPipeTile) {
            PipePluggable p = ((IPipeTile) tile).getPipePluggable(side);
            if (p instanceof IFacadePluggable) {
                // System.out.println("Found facade " + ((FacadePluggable) p).getRenderingMeta());
                return ((IFacadePluggable) p).getCurrentMetadata();
            }
        }
        return 0;
    }

    @Override
    public int isBlockProvidingPowerTo(BlockPos pos, int side) {
        return 0;
    }

    @Override
    public boolean isAirBlock(BlockPos pos) {
        return !(world.getTileEntity(pos) instanceof IPipeTile);
    }

    @Override
    public BiomeGenBase getBiomeGenForCoords(int x, int z) {
        return world.getBiomeGenForCoords(x, z);
    }

    @Override
    public int getHeight() {
        return world.getHeight();
    }

    @Override
    public boolean extendedLevelsInChunkCache() {
        return world.extendedLevelsInChunkCache();
    }

    @Override
    public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean def) {
        return world.isSideSolid(pos, side, def);
    }
}
