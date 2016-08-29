package buildcraft.lib.bpt.task;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.apache.commons.lang3.tuple.Pair;

import buildcraft.lib.bpt.task.DelegateRequested.DelegateFluid;
import buildcraft.lib.bpt.task.DelegateRequested.DelegateItem;
import buildcraft.lib.bpt.task.TaskBuilder.Action;
import buildcraft.lib.bpt.task.TaskBuilder.WhileAction;

public final class TaskDefinition {
    final String id;
    final ImmutableMap<String, DelegateFluid> fluidRequests;
    final ImmutableMap<String, DelegateItem> itemRequests;
    final ImmutableList<TaskDefinition> subTasks;

    final ImmutableList<Pair<RequirementDefinition, TaskDefinition>> taskRequirements;
    final ImmutableList<Pair<RequirementDefinition, Action>> actionRequirements;
    final ImmutableList<Pair<RequirementDefinition, WhileAction>> duringRequirements;

    final ImmutableList<Pair<ICondition, TaskDefinition>> taskConditions;
    final ImmutableList<Pair<ICondition, Action>> actionConditions;

    TaskDefinition(TaskBuilder builder) {
        this.id = builder.id;
        this.fluidRequests = ImmutableMap.copyOf(builder.fluidRequests);
        this.itemRequests = ImmutableMap.copyOf(builder.itemRequests);
        this.subTasks = ImmutableList.copyOf(builder.subTasks);
        this.taskRequirements = ImmutableList.copyOf(builder.taskRequirements);
        this.actionRequirements = ImmutableList.copyOf(builder.actionRequirements);
        this.duringRequirements = ImmutableList.copyOf(builder.duringRequirements);
        this.taskConditions = ImmutableList.copyOf(builder.taskConditions);
        this.actionConditions = ImmutableList.copyOf(builder.actionConditions);
    }

    public TaskUsable createUsableTask() {
        return new TaskUsable(this);
    }
}
