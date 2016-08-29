package buildcraft.lib.bpt.task;

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.util.math.BlockPos;

import buildcraft.api.bpt.IBuilderAccessor;

public class ConditionOr implements ICondition {
    private final ImmutableList<ICondition> conditions;

    public ConditionOr(ICondition a, ICondition b) {
        ImmutableList.Builder<ICondition> builder = ImmutableList.builder();
        if (a instanceof ConditionOr) {
            builder.addAll(((ConditionOr) a).conditions);
        } else {
            builder.add(a);
        }
        if (b instanceof ConditionOr) {
            builder.addAll(((ConditionOr) b).conditions);
        } else {
            builder.add(b);
        }
        this.conditions = builder.build();
    }

    private ConditionOr(List<ICondition> conditions, ICondition additional) {
        ImmutableList.Builder<ICondition> builder = ImmutableList.builder();
        builder.addAll(conditions);
        if (additional instanceof ConditionOr) {
            builder.addAll(((ConditionOr) additional).conditions);
        } else {
            builder.add(additional);
        }
        this.conditions = builder.build();
    }

    @Override
    public boolean resolve(IBuilderAccessor builder, BlockPos buildAt) {
        for (ICondition c : conditions) {
            if (c.resolve(builder, buildAt)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ICondition or(ICondition other) {
        return new ConditionOr(conditions, other);
    }
}
