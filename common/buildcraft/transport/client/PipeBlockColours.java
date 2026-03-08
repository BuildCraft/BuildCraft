package buildcraft.transport.client;

import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.transport.tile.TilePipeHolder;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public enum PipeBlockColours implements BlockColor {
    INSTANCE;

    @Override
//    public int colorMultiplier(BlockState state, @Nullable IBlockAccess world, @Nullable BlockPos pos, int tintIndex) {
    public int getColor(BlockState state, @Nullable BlockAndTintGetter world, @Nullable BlockPos pos, int tintIndex) {
        if (world != null && pos != null) {
            BlockEntity tile = world.getBlockEntity(pos);
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
