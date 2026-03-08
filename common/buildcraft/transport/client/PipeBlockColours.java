package buildcraft.transport.client;

import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.transport.tile.TilePipeHolder;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;

import javax.annotation.Nullable;

public enum PipeBlockColours implements IBlockColor {
    INSTANCE;

    @Override
//    public int colorMultiplier(BlockState state, @Nullable IBlockAccess world, @Nullable BlockPos pos, int tintIndex) {
    public int getColor(BlockState state, @Nullable IBlockDisplayReader world, @Nullable BlockPos pos, int tintIndex) {
        if (world != null && pos != null) {
            TileEntity tile = world.getBlockEntity(pos);
            if (tile instanceof TilePipeHolder) {
                TilePipeHolder tilePipeHolder = (TilePipeHolder) tile;
                Direction side = Direction.from3DDataValue(tintIndex % Direction.values().length);
                PipePluggable pluggable = tilePipeHolder.getPluggable(side);
                if (pluggable != null) {
                    return pluggable.getBlockColor(tintIndex / 6);
                }
            }
        }
        return -1;
    }
}
