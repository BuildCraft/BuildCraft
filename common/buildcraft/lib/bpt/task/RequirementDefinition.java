package buildcraft.lib.bpt.task;

import com.google.common.collect.ImmutableList;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import buildcraft.lib.bpt.task.TaskBuilder.PostTask;
import buildcraft.lib.bpt.task.TaskBuilder.PowerFunction;
import buildcraft.lib.bpt.task.TaskBuilder.RequirementBuilder;

public class RequirementDefinition {
    final Vec3d targetVec;
    final int ticksToWait;
    final long microJoules;
    final ImmutableList<DelegateRequested> toLock;
    final ImmutableList<PostTask> successTasks;
    final ImmutableList<PostTask> completeTasks;
    final ImmutableList<PowerFunction> powerFunctions;

    public RequirementDefinition(RequirementBuilder builder) {
        this.targetVec = builder.targetVec;
        this.ticksToWait = builder.ticksToWait;
        this.microJoules = builder.microJoules;
        this.toLock = ImmutableList.copyOf(builder.toLock);
        this.successTasks = ImmutableList.copyOf(builder.successTasks);
        this.completeTasks = ImmutableList.copyOf(builder.completeTasks);
        this.powerFunctions = ImmutableList.copyOf(builder.powerFunctions);
    }

    public RequirementUsable createUsableRequirement(TaskUsable parent) {
        return new RequirementUsable(parent, this);
    }
}
