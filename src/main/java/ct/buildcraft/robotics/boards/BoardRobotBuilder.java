package ct.buildcraft.robotics.boards;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import ct.buildcraft.api.boards.RedstoneBoardRobot;
import ct.buildcraft.api.boards.RedstoneBoardRobotNBT;
import ct.buildcraft.api.robots.AIRobot;
import ct.buildcraft.api.robots.EntityRobotBase;
import ct.buildcraft.api.robots.ResourceIdBlock;
import ct.buildcraft.builders.snapshot.BlueprintBuilder.RobotBuildTask;
import ct.buildcraft.builders.tile.TileBuilder;
import ct.buildcraft.lib.inventory.filter.ArrayStackOrListFilter;
import ct.buildcraft.lib.misc.NBTUtilBC;
import ct.buildcraft.robotics.BCRoboticsBoards;
import ct.buildcraft.robotics.ai.AIRobotGotoBlock;
import ct.buildcraft.robotics.ai.AIRobotGotoSleep;
import ct.buildcraft.robotics.ai.AIRobotGotoStationAndLoad;
import ct.buildcraft.robotics.ai.AIRobotGotoStationAndUnload;
import ct.buildcraft.robotics.ai.AIRobotRecharge;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * BuildCraft 7 style Builder robot for the modern Builder block.
 * <p>
 * The original 7.1 robot reserved a construction-marker slot, fetched the required stacks, flew to the destination,
 * and committed the slot. The port has no construction marker tile; the active Builder block is the closest equivalent,
 * so this board reserves blueprint positions from nearby Builder blocks and performs the same fetch -> fly -> build loop.
 */
public class BoardRobotBuilder extends RedstoneBoardRobot {
    private static final int MAX_RANGE_SQ = 3 * 64 * 64;
    private static final int RETRY_DELAY = 40;

    private BlockPos builderPos;
    private RobotBuildTask currentTask;
    private LinkedList<ItemStack> requirementsToLookFor;
    private int launchingDelay;

    public BoardRobotBuilder(EntityRobotBase robot) {
        super(robot);
    }

    @Override
    public RedstoneBoardRobotNBT getNBTHandler() {
        return BCRoboticsBoards.getByKey("builder").nbt();
    }

    @Override
    public void update() {
        if (launchingDelay > 0) {
            launchingDelay--;
            return;
        }

        TileBuilder builder = getTargetBuilder();
        if (currentTask == null) {
            if (robot.containsItems()) {
                startDelegateAI(new AIRobotGotoStationAndUnload(robot));
                return;
            }
            builder = reserveClosestTask();
            if (builder == null || currentTask == null) {
                launchingDelay = RETRY_DELAY;
                startDelegateAI(new AIRobotGotoSleep(robot));
                return;
            }
            requirementsToLookFor = new LinkedList<>(currentTask.requirements());
        }

        if (builder == null || !builder.canRobotsBuild()) {
            releaseCurrentTask();
            launchingDelay = RETRY_DELAY;
            startDelegateAI(new AIRobotGotoSleep(robot));
            return;
        }

        if (requirementsToLookFor == null) {
            requirementsToLookFor = new LinkedList<>(currentTask.requirements());
        }

        if (!requirementsToLookFor.isEmpty()) {
            ItemStack stack = requirementsToLookFor.getFirst();
            startDelegateAI(new AIRobotGotoStationAndLoad(robot, new ArrayStackOrListFilter(stack), stack.getCount()));
            return;
        }

        if (robot.getEnergy() - currentTask.energyCost() <= EntityRobotBase.SAFETY_ENERGY) {
            startDelegateAI(new AIRobotRecharge(robot));
            return;
        }

        BlockPos pos = currentTask.pos();
        startDelegateAI(new AIRobotGotoBlock(robot, pos.getX(), pos.getY(), pos.getZ(), 8));
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotGotoStationAndLoad) {
            if (ai.success()) {
                if (requirementsToLookFor != null && !requirementsToLookFor.isEmpty()) {
                    requirementsToLookFor.removeFirst();
                }
            } else {
                releaseCurrentTask();
                startDelegateAI(new AIRobotGotoSleep(robot));
            }
        } else if (ai instanceof AIRobotGotoBlock) {
            TileBuilder builder = getTargetBuilder();
            if (!ai.success() || builder == null || currentTask == null) {
                releaseCurrentTask();
                startDelegateAI(new AIRobotGotoSleep(robot));
                return;
            }
            if (robot.getEnergy() - currentTask.energyCost() <= EntityRobotBase.SAFETY_ENERGY) {
                startDelegateAI(new AIRobotRecharge(robot));
                return;
            }

            robot.getBattery().extractPower(currentTask.energyCost(), currentTask.energyCost());
            boolean built = builder.buildRobotTask(robot, currentTask);
            launchingDelay = built ? Math.max(8, currentTask.requirements().size() * 10) : RETRY_DELAY;
            currentTask = null;
            requirementsToLookFor = null;
            if (!built) {
                startDelegateAI(new AIRobotGotoSleep(robot));
            }
        } else if (ai instanceof AIRobotGotoStationAndUnload) {
            if (!ai.success() && robot.containsItems()) {
                startDelegateAI(new AIRobotGotoSleep(robot));
            }
        } else if (ai instanceof AIRobotRecharge) {
            if (!ai.success()) {
                startDelegateAI(new AIRobotGotoSleep(robot));
            }
        } else if (ai instanceof AIRobotGotoSleep) {
            terminate();
        }
    }

    @Override
    public void end() {
        releaseCurrentTask();
    }

    private TileBuilder reserveClosestTask() {
        List<TileBuilder> builders = new ArrayList<>(TileBuilder.getLoadedBuilders());
        builders.removeIf(builder -> builder == null || builder.getLevel() != robot.level || !builder.canRobotsBuild()
            || robot.blockPosition().distSqr(builder.getBlockPos()) > MAX_RANGE_SQ);
        builders.sort(Comparator.comparingDouble(builder -> robot.blockPosition().distSqr(builder.getBlockPos())));

        for (TileBuilder builder : builders) {
            if (robot.getZoneToWork() != null && !robot.getZoneToWork().contains(Vec3.atCenterOf(builder.getBlockPos()))) {
                continue;
            }
            RobotBuildTask task = builder.reserveRobotBuildTask(robot);
            if (task != null) {
                builderPos = builder.getBlockPos();
                currentTask = task;
                return builder;
            }
        }
        return null;
    }

    private TileBuilder getTargetBuilder() {
        if (builderPos == null || robot.level == null || !robot.level.isLoaded(builderPos)) {
            return null;
        }
        return robot.level.getBlockEntity(builderPos) instanceof TileBuilder builder ? builder : null;
    }

    private void releaseCurrentTask() {
        if (currentTask != null) {
            TileBuilder builder = getTargetBuilder();
            if (builder != null) {
                builder.releaseRobotBuildTask(robot, currentTask);
            } else if (robot.getRegistry() != null) {
                robot.getRegistry().release(new ResourceIdBlock(currentTask.pos()));
            }
        }
        currentTask = null;
        requirementsToLookFor = null;
        builderPos = null;
    }

    @Override
    public boolean canLoadFromNBT() {
        return true;
    }

    @Override
    public void writeSelfToNBT(CompoundTag nbt) {
        super.writeSelfToNBT(nbt);
        nbt.putInt("launchingDelay", launchingDelay);
        if (builderPos != null) {
            nbt.put("builderPos", NbtUtils.writeBlockPos(builderPos));
        }
        if (currentTask != null) {
            nbt.put("currentTask", currentTask.writeToNBT());
        }
        if (requirementsToLookFor != null) {
            nbt.put("requirementsToLookFor", NBTUtilBC.writeObjectList(requirementsToLookFor.stream().map(ItemStack::serializeNBT)));
        }
    }

    @Override
    public void loadSelfFromNBT(CompoundTag nbt) {
        super.loadSelfFromNBT(nbt);
        launchingDelay = nbt.getInt("launchingDelay");
        if (nbt.contains("builderPos")) {
            builderPos = NbtUtils.readBlockPos(nbt.getCompound("builderPos"));
        }
        if (nbt.contains("currentTask")) {
            currentTask = new RobotBuildTask(nbt.getCompound("currentTask"));
        }
        if (nbt.contains("requirementsToLookFor")) {
            requirementsToLookFor = new LinkedList<>(
                NBTUtilBC.readCompoundList(nbt.get("requirementsToLookFor"))
                    .map(ItemStack::of)
                    .toList()
            );
        }
    }
}
