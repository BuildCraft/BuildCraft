package buildcraft.lib.crops;

import buildcraft.api.crops.CropManager;
import buildcraft.api.crops.ICropHandler;
import buildcraft.lib.misc.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.CaveVinesBlock;
import net.minecraft.world.level.block.state.BlockState;

public enum CropHandlerGlowBerries implements ICropHandler {
    INSTANCE;

    @Override
    public boolean isSeed(ItemStack stack) {
        return stack.getItem() == Items.GLOW_BERRIES;
    }

    @Override
    public boolean canSustainPlant(Level world, ItemStack seed, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return ((CaveVinesBlock) Blocks.CAVE_VINES).canSurvive(state, world, pos.relative(Direction.DOWN));
    }

    @Override
    public boolean plantCrop(Level world, Player player, ItemStack seed, BlockPos pos) {
        return BlockUtil.useItemOnBlock(world, player, seed, pos, Direction.DOWN);
    }

    @Override
    public boolean isMature(LevelAccessor access, BlockState state, BlockPos pos) {
        return CaveVines.hasGlowBerries(state);
    }

    @Override
    public CropManager.HarvestResult harvestCrop(Level world, BlockPos pos, ItemStack tool, NonNullList<ItemStack> drops) {
        return CaveVines.use(world.getBlockState(pos), world, pos).consumesAction() ? CropManager.HarvestResult.SUCCESS : CropManager.HarvestResult.FAIL;
    }
}
