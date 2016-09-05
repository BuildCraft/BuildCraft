package buildcraft.lib.bpt.task;

import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;

import buildcraft.api.bpt.IBuilderAccessor;
import buildcraft.api.bpt.IMaterialProvider.IRequestedFluid;
import buildcraft.api.bpt.IMaterialProvider.IRequestedItem;
import buildcraft.lib.bpt.task.DelegateRequested.DelegateFluid;
import buildcraft.lib.bpt.task.DelegateRequested.DelegateItem;
import buildcraft.lib.bpt.task.TaskBuilder.Action;
import buildcraft.lib.bpt.task.TaskBuilder.WhileAction;

public final class TaskUsable {
    public static final TaskUsable NOTHING = TaskDefinition.NOTHING.createUsableTask();
    final TaskUsable root;
    final String id;
    final ImmutableMap<String, DelegateFluid> fluidRequests;
    final ImmutableMap<String, DelegateItem> itemRequests;
    final Map<String, IRequestedFluid> realFluidRequests = new HashMap<>();
    final Map<String, IRequestedItem> realItemRequests = new HashMap<>();

    final ImmutableList<TaskUsable> subTasks;

    final ImmutableList<Pair<RequirementUsable, TaskUsable>> taskRequirements;
    final ImmutableList<Pair<RequirementUsable, Action>> actionRequirements;
    final ImmutableList<Pair<RequirementUsable, WhileAction>> duringRequirements;

    final ImmutableList<Pair<ICondition, Action>> actionConditions;
    final ImmutableList<Pair<ICondition, TaskUsable>> taskConditions;
    boolean conditionsRun = false;
    final BitSet taskConditionResults = new BitSet();
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
        this.actionConditions = ImmutableList.copyOf(definition.actionConditions);
    }

    private TaskUsable mapSubTask(TaskDefinition task) {
        return new TaskUsable(root, task);
    }

    private Pair<ICondition, TaskUsable> mapTaskCondition(Pair<ICondition, TaskDefinition> in) {
        return Pair.of(in.getLeft(), new TaskUsable(root, in.getRight()));
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

    /** @return true if this has finished, false if it has not */
    public boolean tick(IBuilderAccessor builder, BlockPos buildAt) {
        boolean doneAll = true;

        // Setup the requests
        for (String key : fluidRequests.keySet()) {
            DelegateFluid delegate = fluidRequests.get(key);
            IRequestedFluid actual = realFluidRequests.get(key);
            if (actual == null) {
                actual = builder.requestFluid(delegate.getRequested());
                delegate.delegate = actual;
                realFluidRequests.put(key, actual);
            }
        }
        for (String key : itemRequests.keySet()) {
            DelegateItem delegate = itemRequests.get(key);
            IRequestedItem actual = realItemRequests.get(key);
            if (actual == null) {
                actual = builder.requestStack(delegate.getRequested());
                delegate.delegate = actual;
                realItemRequests.put(key, actual);
            }
        }

        // Run the conditions
        if (!conditionsRun) {
            conditionsRun = true;
            for (Pair<ICondition, Action> pair : actionConditions) {
                if (pair.getLeft().resolve(builder, buildAt)) {
                    pair.getRight().call(builder, buildAt);
                    completed.add(pair.getRight());
                } else {
                    failed.add(pair.getRight());
                }
            }

            int i = 0;
            for (Pair<ICondition, TaskUsable> pair : taskConditions) {
                if (pair.getLeft().resolve(builder, buildAt)) {
                    taskConditionResults.set(i);
                } else {
                    failed.add(pair.getRight());
                }
                i++;
            }
        }

        int i = 0;
        for (Pair<ICondition, TaskUsable> pair : taskConditions) {
            if (taskConditionResults.get(i) && !completed.contains(pair.getRight())) {
                if (pair.getRight().tick(builder, buildAt)) {
                    completed.add(pair.getRight());
                } else {
                    doneAll = false;
                }
            }
            i++;
        }

        // requirement based stuffs
        for (Pair<RequirementUsable, Action> pair : actionRequirements) {
            Boolean result = pair.getLeft().tick(builder, buildAt);
            if (result == RequirementUsable.RESULT_FAILED) {
                failed.add(pair.getRight());
            } else if (result == RequirementUsable.RESULT_READY) {
                pair.getRight().call(builder, buildAt);
                completed.add(pair.getRight());
            } else {
                doneAll = false;
            }
        }

        for (Pair<RequirementUsable, WhileAction> pair : duringRequirements) {
            RequirementUsable req = pair.getLeft();
            WhileAction action = pair.getRight();
            if (completed.contains(action) || failed.contains(action)) {
                continue;
            }
            Boolean result = req.tick(builder, buildAt);
            if (result == RequirementUsable.RESULT_FAILED) {
                failed.add(action);
            } else if (result == RequirementUsable.RESULT_READY) {
                action.call(builder, buildAt, req.requiredMicroJoules - req.microJoulesLeft, req.requiredMicroJoules);
                completed.add(action);
            } else if (req.powerFunctionsEvaluated) {
                action.call(builder, buildAt, req.requiredMicroJoules - req.microJoulesLeft, req.requiredMicroJoules);
                doneAll = false;
            } else {
                doneAll = false;
            }
        }

        for (Pair<RequirementUsable, TaskUsable> pair : taskRequirements) {
            RequirementUsable req = pair.getLeft();
            TaskUsable task = pair.getRight();
            Boolean result = req.tick(builder, buildAt);
            if (result == RequirementUsable.RESULT_FAILED) {
                failed.add(task);
            } else if (result == RequirementUsable.RESULT_READY) {
                if (task.tick(builder, buildAt)) {
                    completed.add(task);
                } else {
                    doneAll = false;
                }
            } else {
                doneAll = false;
            }
        }

        return doneAll;
    }
}
