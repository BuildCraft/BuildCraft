package buildcraft.lib.crops;

import buildcraft.api.crops.CropManager;
import buildcraft.api.crops.ICropHandler;
import buildcraft.lib.misc.BlockUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CocoaBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import java.util.Arrays;

public enum CropHandlerCocoa implements ICropHandler {
    INSTANCE;

    @Override
    public boolean isSeed(ItemStack stack) {
        return stack.getItem() == Items.COCOA_BEANS;
    }

    @Override
    public boolean canSustainPlant(World world, ItemStack seed, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return state.is(BlockTags.JUNGLE_LOGS) && !Arrays.stream(Direction.BY_2D_DATA).allMatch(direction -> world.getBlockState(pos.relative(direction)).is(Blocks.COCOA));
    }

    @Override
    public boolean plantCrop(World world, PlayerEntity player, ItemStack seed, BlockPos pos) {
        for (Direction direction : Direction.BY_2D_DATA) {
            if (BlockUtil.useItemOnBlock(world, player, seed, pos, direction)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isMature(IWorld access, BlockState state, BlockPos pos) {
        return state.is(Blocks.COCOA) && state.getValue(CocoaBlock.AGE) >= 2;
    }

    @Override
    public CropManager.HarvestResult harvestCrop(World world, BlockPos pos, ItemStack tool, NonNullList<ItemStack> drops) {
        return CropManager.HarvestResult.PROGRESS;
    }
}
