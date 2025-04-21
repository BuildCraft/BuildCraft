package buildcraft.lib.crops;

import buildcraft.api.crops.CropManager;
import buildcraft.api.crops.ICropHandler;
import buildcraft.lib.misc.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;

public enum CropHandlerCocoa implements ICropHandler {
    INSTANCE;

    @Override
    public boolean isSeed(ItemStack stack) {
        return stack.getItem() == Items.COCOA_BEANS;
    }

    @Override
    public boolean canSustainPlant(Level world, ItemStack seed, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return state.is(BlockTags.JUNGLE_LOGS) && !Arrays.stream(Direction.BY_2D_DATA).allMatch(direction -> world.getBlockState(pos.relative(direction)).is(Blocks.COCOA));
    }

    @Override
    public boolean plantCrop(Level world, Player player, ItemStack seed, BlockPos pos) {
        for (Direction direction : Direction.BY_2D_DATA) {
            if (BlockUtil.useItemOnBlock(world, player, seed, pos, direction)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isMature(LevelAccessor access, BlockState state, BlockPos pos) {
        return state.is(Blocks.COCOA) && state.getValue(CocoaBlock.AGE) >= 2;
    }

    @Override
    public CropManager.HarvestResult harvestCrop(Level world, BlockPos pos, ItemStack tool, NonNullList<ItemStack> drops) {
        return CropManager.HarvestResult.PROGRESS;
    }
}
