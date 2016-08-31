package buildcraft.lib.bpt.task;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;

import buildcraft.api.bpt.IBuilderAccessor;
import buildcraft.lib.bpt.task.DelegateRequested.DelegateFluid;
import buildcraft.lib.bpt.task.DelegateRequested.DelegateItem;
import buildcraft.lib.bpt.task.TaskBuilder.Action;
import buildcraft.lib.bpt.task.TaskBuilder.WhileAction;

public final class TaskUsable {
    final TaskUsable root;
    final String id;
    final ImmutableMap<String, DelegateFluid> fluidRequests;
    final ImmutableMap<String, DelegateItem> itemRequests;
    final ImmutableList<TaskUsable> subTasks;

    final ImmutableList<Pair<RequirementUsable, TaskUsable>> taskRequirements;
    final ImmutableList<Pair<RequirementUsable, Action>> actionRequirements;
    final ImmutableList<Pair<RequirementUsable, WhileAction>> duringRequirements;

    final ImmutableList<Pair<ConditionUsable, TaskUsable>> taskConditions;
    final ImmutableList<Pair<ConditionUsable, Action>> actionConditions;
    final List<Object> completed = new ArrayList<>();
    final List<Object> failed = new ArrayList<>();

    TaskUsable(TaskUsable root, TaskDefinition definition) {
        this.root = root == null ? this : root;
        this.id = definition.id;
        this.fluidRequests = ImmutableMap.copyOf(definition.fluidRequests);
        this.itemRequests = ImmutableMap.copyOf(definition.itemRequests);
        this.subTasks = ImmutableList.copyOf(definition.subTasks.stream().map(this::mapSubTask).collect(Collectors.toList()));
        this.taskRequirements = ImmutableList.copyOf(definition.taskRequirements.stream().map(this::mapTaskRequirement).collect(Collectors.toList()));
        this.actionRequirements = ImmutableList.copyOf(definition.actionRequirements.stream().map(this::mapRequirement).collect(Collectors.toList()));
        this.duringRequirements = ImmutableList.copyOf(definition.duringRequirements.stream().map(this::mapRequirement).collect(Collectors.toList()));
        this.taskConditions = ImmutableList.copyOf(definition.taskConditions.stream().map(this::mapTaskCondition).collect(Collectors.toList()));
        this.actionConditions = ImmutableList.copyOf(definition.actionConditions.stream().map(TaskUsable::mapCondition).collect(Collectors.toList()));
    }

    private TaskUsable mapSubTask(TaskDefinition task) {
        return new TaskUsable(root, task);
    }

    private static <R> Pair<ConditionUsable, R> mapCondition(Pair<ICondition, R> in) {
        return Pair.of(new ConditionUsable(in.getLeft()), in.getRight());
    }

    private Pair<ConditionUsable, TaskUsable> mapTaskCondition(Pair<ICondition, TaskDefinition> in) {
        return Pair.of(new ConditionUsable(in.getLeft()), new TaskUsable(root, in.getRight()));
    }

    private Pair<RequirementUsable, TaskUsable> mapTaskRequirement(Pair<RequirementDefinition, TaskDefinition> in) {
        return Pair.of(new RequirementUsable(root, in.getLeft()), new TaskUsable(root, in.getRight()));
    }

    private <R> Pair<RequirementUsable, R> mapRequirement(Pair<RequirementDefinition, R> in) {
        return Pair.of(new RequirementUsable(root, in.getLeft()), in.getRight());
    }

    public void readFromNbt(NBTTagCompound nbt) {

    }

    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = new NBTTagCompound();

        return nbt;
    }

    private static void readRequirements(List<Pair<RequirementUsable, ?>> list, NBTTagList nbt) {
        for (int i = 0; i < list.size(); i++) {
            RequirementUsable req = list.get(i).getLeft();
            req.readFromNbt(nbt.getCompoundTagAt(i));
        }
    }

    private static NBTTagList writeRequirements(List<Pair<RequirementUsable, ?>> list) {
        NBTTagList nbt = new NBTTagList();
        for (int i = 0; i < list.size(); i++) {
            RequirementUsable req = list.get(i).getLeft();
            nbt.appendTag(req.writeToNbt());
        }
        return nbt;
    }

    private static void readConditions(List<Pair<ConditionUsable, ?>> list, NBTTagList nbt) {
        for (int i = 0; i < list.size(); i++) {
            ConditionUsable req = list.get(i).getLeft();
            req.readFromNbt(nbt.getCompoundTagAt(i));
        }
    }

    private static NBTTagList writeConditions(List<Pair<ConditionUsable, ?>> list) {
        NBTTagList nbt = new NBTTagList();
        for (int i = 0; i < list.size(); i++) {
            ConditionUsable req = list.get(i).getLeft();
            nbt.appendTag(req.writeToNbt());
        }
        return nbt;
    }

    /** @return true if this has finished, false if it has not */
    public boolean tick(IBuilderAccessor builder, BlockPos buildAt) {
        return false;// TODO
    }
}
