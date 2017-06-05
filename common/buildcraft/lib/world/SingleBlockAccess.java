package buildcraft.lib.world;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;

/** An {@link IBlockAccess} for getting the properties of a single {@link IBlockState} at the {@link BlockPos#ORIGIN} */
public class SingleBlockAccess implements IBlockAccess {
    public final IBlockState state;

    public SingleBlockAccess(IBlockState state) {
        this.state = state;
    }

    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        return null;
    }

    @Override
    public int getCombinedLight(BlockPos pos, int lightValue) {
        return lightValue << 4;
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        return BlockPos.ORIGIN.equals(pos) ? state : Blocks.AIR.getDefaultState();
    }

    @Override
    public boolean isAirBlock(BlockPos pos) {
        return getBlockState(pos).getBlock().isAir(state, this, pos);
    }

    @Override
    public Biome getBiome(BlockPos pos) {
        return Biomes.PLAINS;
    }

    @Override
    public int getStrongPower(BlockPos pos, EnumFacing direction) {
        return 0;
    }

    @Override
    public WorldType getWorldType() {
        return WorldType.DEBUG_WORLD;
    }

    @Override
    public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
        if (!BlockPos.ORIGIN.equals(pos)) {
            return _default;
        }
        return state.isSideSolid(this, pos, side);
    }
}
