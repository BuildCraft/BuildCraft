package buildcraft.transport.client;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import buildcraft.api.transport.pluggable.PipePluggable;

import buildcraft.transport.tile.TilePipeHolder;

public enum PipeBlockColours implements IBlockColor {
    INSTANCE;

    @Override
    public int colorMultiplier(IBlockState state, @Nullable IBlockAccess world, @Nullable BlockPos pos, int tintIndex) {
        if (world != null && pos != null) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TilePipeHolder) {
                TilePipeHolder tilePipeHolder = (TilePipeHolder) tile;
                EnumFacing side = EnumFacing.getFront(tintIndex % EnumFacing.VALUES.length);
                PipePluggable pluggable = tilePipeHolder.getPluggable(side);
                if (pluggable != null) {
                    return pluggable.getBlockColor(tintIndex / 6);
                }
            }
        }
        return -1;
    }
}
