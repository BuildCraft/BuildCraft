package buildcraft.lib.bpt.task;

import net.minecraft.util.math.BlockPos;

import buildcraft.api.bpt.IBuilderAccessor;

@FunctionalInterface
public interface ICondition {
    boolean resolve(IBuilderAccessor builder, BlockPos buildAt);

    default ICondition and(ICondition other) {
        return new ConditionAnd(this, other);
    }

    default ICondition or(ICondition other) {
        return new ConditionOr(this, other);
    }

    default ICondition not() {
        return (builder, buildAt) -> {
            return !ICondition.this.resolve(builder, buildAt);
        };
    }
}
