package ct.buildcraft.robotics.boards;

import ct.buildcraft.api.boards.RedstoneBoardRobotNBT;
import ct.buildcraft.api.robots.EntityRobotBase;
import ct.buildcraft.robotics.BCRoboticsBoards;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.ToolActions;

/** BuildCraft 7.1.x miner board port. Fetches a pickaxe and mines reachable ore blocks in the work zone. */
public class BoardRobotMiner extends BoardRobotGenericBreakBlock {
    public BoardRobotMiner(EntityRobotBase robot) {
        super(robot);
    }

    @Override
    public RedstoneBoardRobotNBT getNBTHandler() {
        return BCRoboticsBoards.getByKey("miner").nbt();
    }

    @Override
    public boolean isExpectedTool(ItemStack stack) {
        return !stack.isEmpty()
                && (stack.getItem() instanceof PickaxeItem || stack.canPerformAction(ToolActions.PICKAXE_DIG));
    }

    @Override
    public boolean isExpectedBlock(Level level, BlockPos pos) {
        if (!level.isLoaded(pos)) {
            return false;
        }

        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.getDestroySpeed(level, pos) < 0.0F) {
            return false;
        }
        if (!isOre(state)) {
            return false;
        }

        ItemStack held = robot.getItemBySlot(EquipmentSlot.MAINHAND);
        return isExpectedTool(held) && held.isCorrectToolForDrops(state);
    }

    private static boolean isOre(BlockState state) {
        // Forge ore tags preserve the old OreDictionary "ore*" intent and keep modded ores compatible.
        return state.is(Tags.Blocks.ORES) || state.is(BlockTags.COAL_ORES) || state.is(BlockTags.IRON_ORES)
                || state.is(BlockTags.COPPER_ORES) || state.is(BlockTags.GOLD_ORES) || state.is(BlockTags.REDSTONE_ORES)
                || state.is(BlockTags.EMERALD_ORES) || state.is(BlockTags.LAPIS_ORES) || state.is(BlockTags.DIAMOND_ORES);
    }
}
