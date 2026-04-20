package ct.buildcraft.api.crops;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class CropManager {
    private static List<ICropHandler> handlers = new ArrayList<>();
    private static ICropHandler defaultHandler;

    private CropManager() {

    }

    public static void registerHandler(ICropHandler cropHandler) {
        handlers.add(cropHandler);
    }

    public static void setDefaultHandler(ICropHandler cropHandler) {
        defaultHandler = cropHandler;
    }

    public static ICropHandler getDefaultHandler() {
        return defaultHandler;
    }

    public static boolean isSeed(ItemStack stack) {
        for (ICropHandler cropHandler : handlers) {
            if (cropHandler.isSeed(stack)) {
                return true;
            }
        }
        return defaultHandler.isSeed(stack);
    }

    public static boolean canSustainPlant(Level world, ItemStack seed, BlockPos pos) {
        for (ICropHandler cropHandler : handlers) {
            if (cropHandler.isSeed(seed) && cropHandler.canSustainPlant(world, seed, pos)) {
                return true;
            }
        }
        return defaultHandler.isSeed(seed) && defaultHandler.canSustainPlant(world, seed, pos);
    }

    /** Attempts to plant the crop given by the seed into the world. Also checks to make sure that
     * {@link ICropHandler#isSeed(ItemStack)} is true, and
     * {@link ICropHandler#canSustainPlant(Level, ItemStack, BlockPos)} is true for the position. */
    public static boolean plantCrop(Level world, Player player, ItemStack seed, BlockPos pos) {
        for (ICropHandler cropHandler : handlers) {
            if (cropHandler.isSeed(seed) && cropHandler.canSustainPlant(world, seed, pos) && cropHandler.plantCrop(world, player, seed, pos)) {
                return true;
            }
        }
        if (defaultHandler.isSeed(seed) && defaultHandler.canSustainPlant(world, seed, pos)) {
            return defaultHandler.plantCrop(world, player, seed, pos);
        }
        return false;
    }

    public static boolean isMature(BlockGetter blockAccess, BlockState state, BlockPos pos) {
        for (ICropHandler cropHandler : handlers) {
            if (cropHandler.isMature(blockAccess, state, pos)) {
                return true;
            }
        }
        return defaultHandler.isMature(blockAccess, state, pos);
    }

    public static boolean harvestCrop(Level world, BlockPos pos, NonNullList<ItemStack> drops) {
        BlockState state = world.getBlockState(pos);
        for (ICropHandler cropHandler : handlers) {
            if (cropHandler.isMature(world, state, pos)) {
                return cropHandler.harvestCrop(world, pos, drops);
            }
        }
        return defaultHandler.isMature(world, state, pos) && defaultHandler.harvestCrop(world, pos, drops);
    }

}
