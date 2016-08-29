package buildcraft.lib.bpt.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
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

    /** The builder should use an */
    public static final TaskDefinition REMOVE_EXISTING;
    public static final Action NO_ACTION = (builder, pos) -> {};

    static {
        // TODO: variable power amounts + mid-way tasks! (To allow for breaking)
        REMOVE_EXISTING = new TaskBuilder().build();

        // block place
        final IBlockState bookshelf = Blocks.BOOKSHELF.getDefaultState();

        ICondition stateCheck = condition().isState(BlockPos.ORIGIN, bookshelf).build();

        TaskBuilder pre = new TaskBuilder();
        pre.doIfFalse(//
                stateCheck,//
                removeExisting()//
        );

        TaskBuilder build = new TaskBuilder();
        IRequestedItem reqBook = build.request("main", bookshelf);
        build.doIfFalse(//
                stateCheck,//
                build.subTask("main").doWhen(//
                        build.requirement().lock(reqBook).target(BlockPos.ORIGIN).power(100_000),//
                        (builder, pos) -> {//
                            builder.getWorld().setBlockState(pos, bookshelf);
                        }//
                ).build()//
        );

        // return pre for the clear task, return build for the main building task.

        // bed place

        BlockPos offset = new BlockPos(1, 0, 0);
        ICondition conOff = condition().isState(offset, bookshelf).build();

        pre = new TaskBuilder();
        pre.doIfFalse(//
                stateCheck,//
                removeExisting()//
        );
        pre.doIfFalse(//
                conOff, //
                removeExisting()//
        );

        build = new TaskBuilder();
        final IRequestedItem reqBed = build.request("bed", new ItemStack(Items.BED));
        build.doIfFalse(//
                stateCheck.and(conOff),//
                build.subTask("main").doWhen(//
                        build.requirement().lock(reqBed).target(new Vec3d(1, 1, 0.5)).power(100_000),//
                        (builder, pos) -> {
                            reqBed.use();
                            builder.getWorld().setBlockState(pos, bookshelf);
                            builder.getWorld().setBlockState(pos.add(offset), bookshelf);
                            //
                        }//
                ).build()//
        );

        // single-slot inventory place
        // (only build)
        build = new TaskBuilder();
        IRequested reqBlock = build.request("block", bookshelf);
        IRequested reqStack = build.request("stack", new ItemStack(Items.APPLE));
        PostTask post = build.doIfFalse(//
                condition().isState(BlockPos.ORIGIN, bookshelf).build(),//
                build.subTask("block").doWhen(//
                        build.requirement().lock(reqBlock).target(BlockPos.ORIGIN),//
                        (builder, pos) -> {
                            reqBlock.use();
                            builder.getWorld().setBlockState(pos, bookshelf);
                        }//
                ).build()//
        );
        build.doWhen(//
                build.requirement().lock(reqStack).after(post).target(BlockPos.ORIGIN),//
                (builder, pos) -> {
                    // put it into the tile or something like that
                }//
        );

        // chest place (only build)
        build = new TaskBuilder();
        final IBlockState chest = Blocks.CHEST.getDefaultState();
        ItemStack[] stacks = new ItemStack[27];

        IRequested reqChest = build.request("block", chest);
        IRequestedItem[] reqStacks = new IRequestedItem[27];
        post = build.doIfFalse(//
                condition().isState(BlockPos.ORIGIN, chest).build(),//
                build.subTask("block").doWhen(//
                        build.requirement().lock(reqChest).target(BlockPos.ORIGIN),//
                        (builder, pos) -> {
                            reqChest.use();
                            builder.getWorld().setBlockState(pos, chest);
                            // open chest
                        }//
                ).build()//
        );
        RequirementBuilder requirement = build.requirement().after(post).target(BlockPos.ORIGIN);
        for (int index = 0; index < 27; index++) {
            final int i = index;
            reqStacks[i] = build.request("item#" + i, stacks[i]);
            post = build.subTask("item#" + i).doWhen(//
                    build.requirement().after(post).lock(reqStacks[i]).target(BlockPos.ORIGIN).power(10_000),//
                    (builder, pos) -> {
                        TileEntity tile = builder.getWorld().getTileEntity(pos);
                        if (tile instanceof IInventory) {
                            reqStacks[i].use();
                            ((IInventory) tile).setInventorySlotContents(i, reqStacks[i].getRequested());
                        }
                        // place item into chest
                    }//
            );
            requirement.after(post);
        }
        build.doWhen(requirement, (builder, pos) -> {
            // close chest
        });
    }

    final TaskBuilder parent;
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
        this.parent = parent;
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
        return new TaskBuilder(this, id);
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
     * completed before starting another one. */
    public final class PostTask {
        /** The objects that must have completed before this can be considered to have completed. Support only exists
         * for instances of {@link TaskDefinition}, {@link Action} and {@link WhileAction} - other instances (and null)
         * will throw an exception. These may be equal if only a single object really needs to be complete. */
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

    public final class RequirementBuilder {
        // @formatter:off
        public RequirementBuilder target(Vec3d offset) { return this; }

        public RequirementBuilder target(BlockPos offset) { return this; }

        public RequirementBuilder wait(int ticks) { return this; }

        public RequirementBuilder lock(IRequested req) { return this; }

        public RequirementBuilder power(long microJoules) { return this; }

        public RequirementBuilder power(PowerFunction function) { return this; }

        public RequirementBuilder after(PostTask post) { return this; }

        public RequirementDefinition build() { return null; }
        // @formatter:on
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
