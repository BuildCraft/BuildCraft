// STUB(R.Chen): Minecraft 1.12 IBlockColor → bridges to Fabric 1.20.1 BlockColorProvider.
package net.minecraft.client.renderer.color;

import net.minecraft.block.BlockState;
import net.minecraft.client.color.block.BlockColorProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

public interface IBlockColor extends BlockColorProvider {
    int colorMultiplier(BlockState state, BlockRenderView world, BlockPos pos, int tintIndex);

    @Override
    default int getColor(BlockState state, BlockRenderView world, BlockPos pos, int tintIndex) {
        return colorMultiplier(state, world, pos, tintIndex);
    }
}
