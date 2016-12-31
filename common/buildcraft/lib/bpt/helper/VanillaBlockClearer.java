package buildcraft.lib.bpt.helper;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import buildcraft.api.bpt.IBuilderAccessor;
import buildcraft.api.bpt.Schematic.DefaultBptActions;
import buildcraft.api.bpt.Schematic.EnumPreBuildAction;
import buildcraft.api.bpt.Schematic.PreBuildAction;

import buildcraft.lib.bpt.task.ICondition;
import buildcraft.lib.bpt.task.TaskBuilder;
import buildcraft.lib.bpt.task.TaskBuilder.Action;
import buildcraft.lib.bpt.task.TaskBuilder.PowerFunction;
import buildcraft.lib.bpt.task.TaskDefinition;
import buildcraft.lib.bpt.task.TaskUsable;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.EntityUtil;

@Deprecated
public enum VanillaBlockClearer implements PreBuildAction {
    DESTORY_ITEMS(false),
    COLLECT_ITEMS(true);

    private final TaskDefinition defintion;

    private VanillaBlockClearer(final boolean shouldDrop) {
        TaskBuilder task = new TaskBuilder();
        ICondition con = (builder, pos) -> {
            return builder.getWorld().isAirBlock(pos);
        };
        TaskBuilder ifNotAir = task.subTask("ifNotAir");
        PowerFunction reqPower = (builder, pos) -> BlockUtil.computeBlockBreakPower(builder.getWorld(), pos);

        Action action = (builder, pos) -> {
            builder.getWorld().destroyBlock(pos, shouldDrop);
            for (ItemStack stack : EntityUtil.collectItems(builder.getWorld(), pos, 2)) {
                builder.returnItems(pos, stack);
            }
        };

        ifNotAir.doWhen(task.requirement().power(reqPower).target(BlockPos.ORIGIN), action);
        task.doIfFalse(con, ifNotAir.build());
        defintion = task.build();
    }

    @Override
    public EnumPreBuildAction getType() {
        // Custom so it will not be replaced with itself
        return EnumPreBuildAction.CUSTOM_REMOVAL;
    }

    @Override
    public TaskUsable getTask(IBuilderAccessor builder, BlockPos pos) {
        return defintion.createUsableTask();
    }

    @Override
    public int getTimeCost() {
        return 7;
    }
}
