package buildcraft.lib.bpt.task;

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.util.math.BlockPos;

import buildcraft.api.bpt.IBuilderAccessor;

public class ConditionAnd implements ICondition {
    private final ImmutableList<ICondition> conditions;

    public ConditionAnd(List<ICondition> conditions) {
        ImmutableList.Builder<ICondition> builder = ImmutableList.builder();
        for (ICondition con : conditions) {
            if (con instanceof ConditionAnd) {
                builder.addAll(((ConditionAnd) con).conditions);
            } else {
                builder.add(con);
            }
        }
        this.conditions = builder.build();
    }

    public ConditionAnd(ICondition a, ICondition b) {
        ImmutableList.Builder<ICondition> builder = ImmutableList.builder();
        if (a instanceof ConditionAnd) {
            builder.addAll(((ConditionAnd) a).conditions);
        } else {
            builder.add(a);
        }
        if (b instanceof ConditionAnd) {
            builder.addAll(((ConditionAnd) b).conditions);
        } else {
            builder.add(b);
        }
        this.conditions = builder.build();
    }

    private ConditionAnd(List<ICondition> conditions, ICondition additional) {
        ImmutableList.Builder<ICondition> builder = ImmutableList.builder();
        builder.addAll(conditions);
        if (additional instanceof ConditionAnd) {
            builder.addAll(((ConditionAnd) additional).conditions);
        } else {
            builder.add(additional);
        }
        this.conditions = builder.build();
    }

    @Override
    public boolean resolve(IBuilderAccessor builder, BlockPos buildAt) {
        for (ICondition c : conditions) {
            if (!c.resolve(builder, buildAt)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ICondition and(ICondition other) {
        return new ConditionAnd(conditions, other);
    }
}
