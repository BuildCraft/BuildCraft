package buildcraft.lib.bpt.task;

import java.util.BitSet;

import com.google.common.collect.ImmutableList;

import org.apache.commons.lang3.tuple.MutablePair;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import buildcraft.api.bpt.IBuilderAccessor;
import buildcraft.api.bpt.IMaterialProvider.IRequested;
import buildcraft.lib.bpt.task.DelegateRequested.DelegateFluid;
import buildcraft.lib.bpt.task.DelegateRequested.DelegateItem;
import buildcraft.lib.bpt.task.TaskBuilder.PostTask;
import buildcraft.lib.bpt.task.TaskBuilder.PowerFunction;

public class RequirementUsable {
    public static final Boolean RESULT_NOT_YET = null;
    public static final Boolean RESULT_FAILED = Boolean.FALSE;
    public static final Boolean RESULT_READY = Boolean.TRUE;

    final TaskUsable root;
    final Vec3d targetVec;
    int ticksToWait;
    /** The TOTAL amount of micro joules that will be required to build this. Never decremented */
    long requiredMicroJoules;
    /** The amount of microjoules left to request. Never more than requiredMicroJoules. When this reaches 0 the power
     * requirement has been satisfied. */
    long microJoulesLeft;
    final ImmutableList<MutablePair<DelegateRequested, IRequested>> toLock;
    final ImmutableList<PostTask> successTasks;
    final ImmutableList<PostTask> completeTasks;
    final ImmutableList<PowerFunction> powerFunctions;
    final BitSet hasAnimStarted = new BitSet();
    final BitSet successTaskComplete = new BitSet();
    final BitSet completeTaskComplete = new BitSet();
    boolean powerFunctionsEvaluated = false;

    public RequirementUsable(TaskUsable root, RequirementDefinition requirement) {
        this.root = root;
        this.targetVec = requirement.targetVec;
        this.ticksToWait = requirement.ticksToWait;
        this.microJoulesLeft = requirement.microJoules;
        requiredMicroJoules = microJoulesLeft;
        ImmutableList.Builder<MutablePair<DelegateRequested, IRequested>> builder = ImmutableList.builder();
        for (DelegateRequested req : requirement.toLock) {
            builder.add(MutablePair.of(req, null));
        }
        this.toLock = builder.build();
        this.successTasks = ImmutableList.copyOf(requirement.successTasks);
        this.completeTasks = ImmutableList.copyOf(requirement.completeTasks);
        this.powerFunctions = ImmutableList.copyOf(requirement.powerFunctions);
    }

    public void readFromNbt(NBTTagCompound nbt) {
        // TODO readNbt
    }

    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = new NBTTagCompound();
        // TODO writeNbt
        return nbt;
    }

    /** Requirement stages:
     * <ol>
     * <li>Wait for other tasks to complete</li>
     * <li>Lock some {@link IRequested} requests that will be used.</li>
     * <li>Wait an arbitrary length of time</li>
     * <li>Move the builder active head position to a specified location (there can only be one at a time, but some
     * builders may do this instantly)</li>
     * <li>Receive a certain amount of power.</li>
     * </ol>
    */

    /** @return null if the requirements have not quite been met yet, Boolean.FALSE if the requirements can *never* be
     *         met, and Boolean.TRUE if the requirements have been met. */
    public Boolean tick(IBuilderAccessor builder, BlockPos buildAt) {
        // stage 1 -- tasks
        for (int i = 0; i < successTasks.size(); i++) {
            if (successTaskComplete.get(i)) {
                continue;
            }
            PostTask task = successTasks.get(i);
            int comp = 0;
            if (!root.completed.contains(task.toCompleteA)) return RESULT_NOT_YET;
            else comp++;
            if (!root.completed.contains(task.toCompleteB)) return RESULT_NOT_YET;
            else comp++;
            if (root.failed.contains(task.toCompleteA)) return RESULT_FAILED;
            if (root.failed.contains(task.toCompleteB)) return RESULT_FAILED;
            if (comp == 2) {
                successTaskComplete.set(i);
            }
        }
        for (int i = 0; i < completeTasks.size(); i++) {
            PostTask task = completeTasks.get(i);
            int comp = 0;
            if (!root.completed.contains(task.toCompleteA)) return RESULT_NOT_YET;
            else comp = 1;
            if (!root.completed.contains(task.toCompleteB)) return RESULT_NOT_YET;
            else comp++;
            if (comp == 2) {
                completeTaskComplete.set(i);
            } else {
                comp = 0;
                if (root.failed.contains(task.toCompleteA)) comp = 1;
                if (root.failed.contains(task.toCompleteB)) comp++;
                if (comp == 2) {
                    completeTaskComplete.set(i);
                }
            }
        }

        // stage 2 -- requests
        boolean all = true;

        int i = 0;
        for (MutablePair<DelegateRequested, IRequested> pair : toLock) {
            DelegateRequested delegate = pair.left;
            if (pair.right == null) {
                if (delegate instanceof DelegateItem) {
                    pair.right = builder.requestStack(((DelegateItem) delegate).getRequested());
                } else {
                    pair.right = builder.requestFluid(((DelegateFluid) delegate).getRequested());
                }
            }
            if (!pair.right.lock()) {
                all = false;
            } else {
                boolean has = hasAnimStarted.get(i);
                if (!has) {
                    hasAnimStarted.set(i);
                    // TODO: remove this! (it should be in each task definition)
                    int time;
                    if (delegate instanceof DelegateItem) {
                        time = builder.startItemStackAnimation(targetVec, ((DelegateItem) delegate).getRequested(), 0);
                    } else {
                        time = builder.startFluidAnimation(targetVec, ((DelegateFluid) delegate).getRequested(), 0)[1];
                    }
                    ticksToWait = Math.max(ticksToWait, time);
                }
            }
            pair.left.delegate = pair.right;
            i++;
        }

        if (all == false) {
            return RESULT_NOT_YET;
        }

        // stage 3 -- waiting
        if (ticksToWait > 0) {
            ticksToWait--;
            return RESULT_NOT_YET;
        }

        // stage 4 -- moving
        if (targetVec != null) {
            if (!builder.target(targetVec, root)) {
                return RESULT_NOT_YET;
            }
        }

        // stage 5 -- power
        if (!powerFunctionsEvaluated) {
            powerFunctionsEvaluated = true;
            for (PowerFunction func : powerFunctions) {
                long req = func.getRequired(builder, buildAt);
                requiredMicroJoules += req;
                microJoulesLeft += req;
            }
        }
        microJoulesLeft -= builder.drainPower(microJoulesLeft, root);
        if (microJoulesLeft == 0) {
            return RESULT_READY;
        } else {
            return RESULT_NOT_YET;
        }
    }
}
