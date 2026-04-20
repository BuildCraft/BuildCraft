package ct.buildcraft.transport.client;

import javax.annotation.Nullable;

import ct.buildcraft.api.transport.pluggable.PipePluggable;
import ct.buildcraft.transport.tile.TilePipeHolder;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public enum PipeBlockColours implements BlockColor {
    INSTANCE;

    @Override
    public int getColor(BlockState state, @Nullable BlockAndTintGetter world, @Nullable BlockPos pos, int tintIndex) {
        if (world != null && pos != null) {
            BlockEntity tile = world.getBlockEntity(pos);
            if (tile instanceof TilePipeHolder) {
                TilePipeHolder tilePipeHolder = (TilePipeHolder) tile;
                Direction side = Direction.from3DDataValue(tintIndex % Direction.values().length);
                PipePluggable pluggable = tilePipeHolder.getPluggable(side);
                if (pluggable != PipePluggable.EMPTY) {
                    return pluggable.getBlockColor(tintIndex / 6);
                }
            }
        }
        return -1;
    }
}
