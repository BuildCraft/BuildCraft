package buildcraft.lib.bpt.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import net.minecraftforge.fluids.FluidStack;

import buildcraft.api.bpt.IBuilderAccessor;
import buildcraft.api.bpt.IMaterialProvider.IRequested;
import buildcraft.api.bpt.IMaterialProvider.IRequestedFluid;
import buildcraft.api.bpt.IMaterialProvider.IRequestedItem;
import buildcraft.lib.bpt.task.DelegateRequested.DelegateFluid;
import buildcraft.lib.bpt.task.DelegateRequested.DelegateItem;
import buildcraft.lib.misc.StackUtil;

public final class TaskBuilder {
    public static final TaskDefinition REMOVE_EXISTING = new TaskBuilder().build();

    final TaskBuilder root;
    final String id;
    final Map<String, DelegateFluid> fluidRequests = new HashMap<>();
    final Map<String, DelegateItem> itemRequests = new HashMap<>();
    final List<TaskDefinition> subTasks = new ArrayList<>();

    final List<Pair<RequirementDefinition, TaskDefinition>> taskRequirements = new ArrayList<>();
    final List<Pair<RequirementDefinition, Action>> actionRequirements = new ArrayList<>();
    final List<Pair<RequirementDefinition, WhileAction>> duringRequirements = new ArrayList<>();

    final List<Pair<ICondition, TaskDefinition>> taskConditions = new ArrayList<>();
    final List<Pair<ICondition, Action>> actionConditions = new ArrayList<>();

    public TaskBuilder() {
        this(null, "root");
    }

    private TaskBuilder(TaskBuilder parent, String id) {
        this.root = parent == null ? this : parent;
        this.id = id;
    }

    public IRequestedItem request(String requestId, ItemStack stack) {
        if (itemRequests.containsKey(requestId)) {
            throw new IllegalArgumentException("Already had an item request of " + requestId);
        }
        DelegateItem delegate = new DelegateItem(stack);
        itemRequests.put(requestId, delegate);
        return delegate;
    }

    public IRequestedItem request(String requestId, IBlockState state) {
        return request(requestId, StackUtil.getItemStackForState(state));
    }

    public IRequestedFluid request(String requestId, FluidStack fluid) {
        if (itemRequests.containsKey(requestId)) {
            throw new IllegalArgumentException("Already had a fluid request of " + requestId);
        }
        DelegateFluid delegate = new DelegateFluid(fluid);
        fluidRequests.put(requestId, delegate);
        return delegate;
    }

    public TaskBuilder subTask(String id) {
        return new TaskBuilder(root, id);
    }

    public RequirementBuilder requirement() {
        return new RequirementBuilder();
    }

    public static ConditionBuilder condition() {
        return new ConditionBuilder();
    }

    public static TaskDefinition removeExisting() {
        return REMOVE_EXISTING;
    }

    public TaskBuilder doAlways(Action action) {
        doWhen(requirement(), action);
        return this;
    }

    public TaskBuilder doAlways(TaskDefinition task) {
        doWhen(requirement(), task);
        return this;
    }

    public PostTask doWhen(RequirementBuilder requirement, Action action) {
        actionRequirements.add(Pair.of(requirement.build(), action));
        return new PostTask(action);
    }

    public PostTask doWhen(RequirementBuilder requirement, TaskDefinition task) {
        taskRequirements.add(Pair.of(requirement.build(), task));
        return new PostTask(task);
    }

    public PostTask doWhile(RequirementBuilder requirement, WhileAction action) {
        duringRequirements.add(Pair.of(requirement.build(), action));
        return new PostTask(action);
    }

    public PostTask doIfTrue(ICondition con, Action action) {
        actionConditions.add(Pair.of(con, action));
        return new PostTask(action);
    }

    public PostTask doIfFalse(ICondition con, Action action) {
        actionConditions.add(Pair.of(con.not(), action));
        return new PostTask(action);
    }

    public PostTask doIf(ICondition con, Action ifTrue, Action ifFalse) {
        actionConditions.add(Pair.of(con, ifTrue));
        actionConditions.add(Pair.of(con.not(), ifFalse));
        return new PostTask(ifTrue, ifFalse);
    }

    public PostTask doIfTrue(ICondition con, TaskDefinition task) {
        taskConditions.add(Pair.of(con, task));
        return new PostTask(task);
    }

    public PostTask doIfFalse(ICondition con, TaskDefinition task) {
        taskConditions.add(Pair.of(con.not(), task));
        return new PostTask(task);
    }

    public PostTask doIf(ICondition con, TaskDefinition ifTrue, TaskDefinition ifFalse) {
        taskConditions.add(Pair.of(con, ifTrue));
        taskConditions.add(Pair.of(con.not(), ifFalse));
        return new PostTask(ifTrue, ifFalse);
    }

    public TaskDefinition build() {
        return new TaskDefinition(this);
    }

    /** Defines a requirement link that requires a {@link TaskDefinition}, {@link Action} or {@link WhileAction} to have
     * completed before starting another one. Note that if this is returned from
     * {@link TaskBuilder#doIf(ICondition, Action, Action)} or
     * {@link TaskBuilder#doIf(ICondition, TaskDefinition, TaskDefinition)} then this only requires one of the 2 tasks
     * to have completed, not both. */
    public final class PostTask {
        /** Either of the objects that must have completed before this can be considered to have completed. Support only
         * exists for instances of {@link TaskDefinition}, {@link Action} and {@link WhileAction} - other instances (and
         * null) will throw an exception. These may be equal if only a single object really needs to be complete. */
        public final Object toCompleteA, toCompleteB;

        private PostTask(Object toComplete) {
            this(toComplete, toComplete);
        }

        private PostTask(Object toCompleteA, Object toCompleteB) {
            this.toCompleteA = toCompleteA;
            this.toCompleteB = toCompleteB;
        }

        /** This is provided to simplify task building, it is just a call to the main task to build. (So you can do
         * <p>
         * <code>
         * TaskBuilder build = new TaskBuilder();<br>
         * Task t = build.doWhile(requirement, action).build();</code>
         * <p>
         * rather than
         * <p>
         * <code>
         * TaskBuilder build = new TaskBuilder();<br>
         * build.doWhile(requirement, action);<br>
         * Task t = build.build();
         * </code> */
        public TaskDefinition build() {
            return TaskBuilder.this.build();
        }
    }

    /** Implements requirements. There are 5 stages:
     * <ol>
     * <li>Wait for other tasks to complete</li>
     * <li>Lock some {@link IRequested} requests that will be used.</li>
     * <li>Wait an arbitrary length of time</li>
     * <li>Move the builder active head position to a specified location (there can only be one at a time, but some
     * builders may do this instantly)</li>
     * <li>Receive a certain amount of power.</li>
     * </ol>
    */
    public final class RequirementBuilder {
        Vec3d targetVec;
        int ticksToWait = 0;
        long microJoules;
        final List<DelegateRequested> toLock = new ArrayList<>();
        final List<PowerFunction> powerFunctions = new ArrayList<>();
        final List<PostTask> successTasks = new ArrayList<>();
        final List<PostTask> completeTasks = new ArrayList<>();

        /** Targets the builder at a particular place. These will be waited *after* the tasks have been completed and
         * requests have been satisfied, and after the {@link #waitTicks(int)} time has been waited. (so stage 4) */
        public RequirementBuilder target(Vec3d offset) {
            targetVec = offset;
            return this;
        }

        /** Targets the builder at a particular place. These will be waited *after* the tasks have been completed and
         * requests have been satisfied, and after the {@link #waitTicks(int)} time has been waited. (so stage 4) */
        public RequirementBuilder target(BlockPos offset) {
            return target(new Vec3d(offset).addVector(0.5, 0.5, 0.5));
        }

        /** Waits a certain number of ticks. These will be waited *after* the tasks have been completed and requests
         * have been satisfied (so stage 3) */
        public RequirementBuilder waitTicks(int ticks) {
            ticksToWait += ticks;
            if (ticksToWait < 0) {
                ticksToWait = 0;
            }
            return this;
        }

        /** Tries to lock a particular {@link IRequested} item or fluid. This is requested *after* the tasks have been
         * completed. (so stage 2) */
        public RequirementBuilder lock(IRequested req) {
            toLock.add((DelegateRequested) req);
            return this;
        }

        /** Tries to acquire some power for building. This is requested *after* the tasks have been completed, the
         * requests have been locked, the {@link #waitTicks(int)} time has been waited, and the builder has been
         * positioned at the target. (so stage 5) */
        public RequirementBuilder power(long microJoules) {
            this.microJoules += microJoules;
            return this;
        }

        /** Tries to acquire some power for building. This is requested *after* the tasks have been completed, the
         * requests have been locked, the {@link #waitTicks(int)} time has been waited, and the builder has been
         * positioned at the target. (so stage 5).
         * <p>
         * The actual amount of power requested is evaluated lazily at the last possible opportunity. It is evaluated
         * only once per schematic per block. */
        public RequirementBuilder power(PowerFunction function) {
            powerFunctions.add(function);
            return this;
        }

        /** Waits for another task to complete. This is the first part of the requirement, so no other requirements will
         * start until either the given task completes (in which case it will move on if all tasks have been satisfied)
         * or it will fail early if some of the required tasks failed to complete and requireSuccess is true.
         * 
         * @param post The task that should be waited on.
         * @param requireSuccess If true then the task *must* be called and completed (for example the conditional in
         *            {@link TaskBuilder}.doIfTrue or {@link TaskBuilder}.doIfFalse), but if its false then the task
         *            must have either been completed or defined to not run. */
        public RequirementBuilder after(PostTask post, boolean requireSuccess) {
            if (requireSuccess) {
                successTasks.add(post);
            } else {
                completeTasks.add(post);
            }
            return this;
        }

        public RequirementDefinition build() {
            return new RequirementDefinition(this);
        }
    }

    public static final class ConditionBuilder {
        private final List<ICondition> conditions = new ArrayList<>();

        public ConditionBuilder isAir(BlockPos offset) {
            return isState(offset, Blocks.AIR.getDefaultState());
        }

        public ConditionBuilder isState(BlockPos offset, IBlockState state) {
            conditions.add((builder, buildAt) -> {
                return builder.getWorld().getBlockState(buildAt.add(offset)).equals(state);
            });
            return this;
        }

        public ICondition and(ICondition other) {
            return build().and(other);
        }

        public ICondition or(ICondition other) {
            return build().or(other);
        }

        public ICondition build() {
            return new ConditionAnd(conditions);
        }
    }

    /** Defines some action that will only be called once for each time it has been registered. */
    @FunctionalInterface
    public interface Action {
        public void call(IBuilderAccessor builder, BlockPos buildAt);
    }

    /** Defines some action that is called for every tick after the task holding it starts, and until all of its
     * requirements have been satisfied. Note that this is *only* called when all requirements other than power have
     * been satisfied. */
    @FunctionalInterface
    public interface WhileAction {
        public void call(IBuilderAccessor builder, BlockPos buildAt, long powerSoFar, long powerRequired);
    }

    /** Defines a way of requiring an amount of power that is determined for each block rather than hardcoded for every
     * schematic */
    @FunctionalInterface
    public interface PowerFunction {
        public long getRequired(IBuilderAccessor builder, BlockPos buildAt);
    }
}
