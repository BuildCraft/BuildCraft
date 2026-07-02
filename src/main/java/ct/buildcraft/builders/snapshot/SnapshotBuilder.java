/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.builders.snapshot;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.common.collect.ImmutableList;

import ct.buildcraft.api.core.BCLog;
import ct.buildcraft.api.mj.MjAPI;
import ct.buildcraft.lib.misc.BlockUtil;
import ct.buildcraft.lib.misc.MessageUtil;
import ct.buildcraft.lib.misc.NBTUtilBC;
import ct.buildcraft.lib.misc.VecUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Message;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public abstract class SnapshotBuilder<T extends ITileForSnapshotBuilder> implements INBTSerializable<CompoundTag> {
    private static final int MAX_QUEUE_SIZE = 16;
    protected static final byte CHECK_RESULT_UNKNOWN = 0;
    protected static final byte CHECK_RESULT_CORRECT = 1;
    protected static final byte CHECK_RESULT_TO_BREAK = 2;
    protected static final byte CHECK_RESULT_TO_PLACE = 3;
    private static final byte REQUIRED_UNKNOWN = 0;
    private static final byte REQUIRED_TRUE = 1;
    private static final byte REQUIRED_FALSE = 2;
    private static final int CHECKS_PER_TICK = 10;
    private static final long MAX_POWER_PER_TICK = 256 * MjAPI.MJ;

    protected final T tile;
	private BlockPositionSource blockPosSource;
    private final GameEventListener worldEventListener = new GameEventListener() {
    	@Override
    	public boolean handleEventsImmediately() {
    		return true;
    	}
    	@Override
    	public PositionSource getListenerSource() {
    		if(blockPosSource == null)
    			blockPosSource = new BlockPositionSource(tile.getBuilderPos());
    		return blockPosSource;
    	}
    	@Override
    	public int getListenerRadius() {
    		return 64;
    	}
    	@Override
    	public boolean handleGameEvent(ServerLevel level, Message msg) {
    		GameEvent e = msg.gameEvent();
    		if(e == GameEvent.BLOCK_PLACE || e == GameEvent.BLOCK_DESTROY) {
    			Vec3 pos = msg.source();
                if (tile.getBuilder() == SnapshotBuilder.this && getBuildingInfo() != null && getBuildingInfo().box.contains(pos)) {
                    if (check(new BlockPos(pos))) {
                        afterChecks();
                    }
                }
                return true;
    		}
    		return false;
    	}
    };
    private final Queue<BreakTask> breakTasks = new ArrayDeque<>();
    public final Queue<BreakTask> clientBreakTasks = new ArrayDeque<>();
   
    public final Queue<BreakTask> prevClientBreakTasks = new ArrayDeque<>();
    private final Queue<PlaceTask> placeTasks = new ArrayDeque<>();
    public final Queue<PlaceTask> clientPlaceTasks = new ArrayDeque<>();
   
    public final Queue<PlaceTask> prevClientPlaceTasks = new ArrayDeque<>();
   
    protected byte[] checkResults;
    private byte[] requiredCache;
    private int[] breakOrder;
    private int[] placeOrder;
    private int[] checkOrder;
    private int currentCheckIndex;
    public Vec3 robotPos = null;
    public Vec3 prevRobotPos = null;
    public int leftToBreak = 0;
    public int leftToPlace = 0;

   
    protected SnapshotBuilder(T tile) {
        this.tile = tile;
    }

    protected abstract Snapshot.BuildingInfo getBuildingInfo();

    public void validate() {
    	Level level = tile.getWorldBC();
        if (!level.isClientSide) {
//        	BlockPos pos = tile.getBuilderPos();
//        	level.getChunk(pos).getEventDispatcher(SectionPos.blockToSectionCoord(pos.getY())).register(worldEventListener);
        }
    }

    public void invalidate() {
    	Level level = tile.getWorldBC();
        if (!level.isClientSide) {
//        	BlockPos pos = tile.getBuilderPos();
//        	level.getChunk(pos).getEventDispatcher(SectionPos.blockToSectionCoord(pos.getY())).unregister(worldEventListener);
        }
    }

    protected abstract boolean isAir(BlockPos blockPos);

    protected abstract boolean canPlace(BlockPos blockPos);

    protected abstract boolean isReadyToPlace(BlockPos blockPos);

    protected abstract boolean hasEnoughToPlaceItems(BlockPos blockPos);

    protected abstract List<ItemStack> getToPlaceItems(BlockPos blockPos);

    /**
     * @return true if task done successfully, false otherwise
     */
    protected abstract boolean doPlaceTask(PlaceTask placeTask);

    /**
     * Executed if break task failed
     */
    private void cancelBreakTask(BreakTask breakTask) {
        tile.getBattery().addPower(
            Math.min(breakTask.power, tile.getBattery().getCapacity() - tile.getBattery().getStored()),
            FluidAction.EXECUTE
        );
    }

    /**
     * Executed if {@link #doPlaceTask} failed
     */
    protected void cancelPlaceTask(PlaceTask placeTask) {
        tile.getBattery().addPower(
            Math.min(placeTask.power, tile.getBattery().getCapacity() - tile.getBattery().getStored()),
            FluidAction.EXECUTE
        );
    }

    /**
     * @return true if block in wold is correct (is not to break) according to snapshot, false otherwise
     */
    protected abstract boolean isBlockCorrect(BlockPos blockPos);

    /**
     * @return Pos where flying item should be rendered
     */
    public Vec3 getPlaceTaskItemPos(PlaceTask placeTask) {
        Vec3 height = Vec3.atLowerCornerOf(placeTask.pos.subtract(tile.getBuilderPos()));
        double progress = placeTask.power * 1D / placeTask.getTarget();
        return Vec3.atLowerCornerOf(tile.getBuilderPos())
            .add(height.scale(progress))
            .add(new Vec3(0, Math.sin(progress * Math.PI) * (Math.abs(height.y) + 1), 0))
            .add(new Vec3(0.5, 1, 0.5));
    }

    public void updateSnapshot() {
        tile.getWorldBC().getProfiler().push("init");
        checkResults = new byte[
            getBuildingInfo().box.size().getX() *
                getBuildingInfo().box.size().getY() *
                getBuildingInfo().box.size().getZ()
            ];
        Arrays.fill(checkResults, CHECK_RESULT_UNKNOWN);
        requiredCache = new byte[
            getBuildingInfo().box.size().getX() *
                getBuildingInfo().box.size().getY() *
                getBuildingInfo().box.size().getZ()
            ];
        Arrays.fill(requiredCache, REQUIRED_UNKNOWN);
        breakOrder = getBuildingInfo().box.getBlocksInArea().stream()
            .sorted(BlockUtil.uniqueBlockPosComparator(Comparator.comparingDouble(blockPos ->
                Math.pow(blockPos.getX() - getBuildingInfo().box.center().getX(), 2) +
                    Math.pow(blockPos.getZ() - getBuildingInfo().box.center().getZ(), 2) +
                    100_000 - Math.abs(blockPos.getY() - tile.getBuilderPos().getY()) * 100_000
            )))
            .mapToInt(this::posToIndex)
            .toArray();
        placeOrder = getBuildingInfo().box.getBlocksInArea().stream()
            .sorted(BlockUtil.uniqueBlockPosComparator(Comparator.comparingDouble(blockPos ->
                100_000 - (Math.pow(blockPos.getX() - tile.getBuilderPos().getX(), 2) +
                    Math.pow(blockPos.getZ() - tile.getBuilderPos().getZ(), 2)) +
                    Math.abs(blockPos.getY() - tile.getBuilderPos().getY()) * 100_000
            )))
            .mapToInt(this::posToIndex)
            .toArray();
        checkOrder = getBuildingInfo().box.getBlocksInArea().stream()
            .sorted(BlockUtil.uniqueBlockPosComparator(Comparator.comparingDouble(blockPos ->
                Math.pow(blockPos.getX() - getBuildingInfo().box.center().getX(), 2) +
                    Math.pow(blockPos.getY() - getBuildingInfo().box.center().getY(), 2) +
                    Math.pow(blockPos.getZ() - getBuildingInfo().box.center().getZ(), 2)
            )))
            .mapToInt(this::posToIndex)
            .toArray();
        tile.getWorldBC().getProfiler().pop();
    }

    public void resourcesChanged() {
        Arrays.fill(requiredCache, REQUIRED_UNKNOWN);
    }

    public void cancel() {
        breakTasks.forEach(this::cancelBreakTask);
        placeTasks.forEach(this::cancelPlaceTask);
        breakTasks.clear();
        clientBreakTasks.clear();
        prevClientBreakTasks.clear();
        placeTasks.clear();
        clientPlaceTasks.clear();
        prevClientPlaceTasks.clear();
        checkResults = null;
        requiredCache = null;
        breakOrder = null;
        placeOrder = null;
        checkOrder = null;
        currentCheckIndex = 0;
        robotPos = null;
        prevRobotPos = null;
        leftToBreak = 0;
        leftToPlace = 0;
    }

    /**
     * @return true is building is finished, false otherwise
     */
    @SuppressWarnings("resource")
	public boolean tick() {
    	Level level = tile.getWorldBC();
        if (level.isClientSide) {
            prevClientBreakTasks.clear();
            prevClientBreakTasks.addAll(clientBreakTasks);
            clientBreakTasks.clear();
            clientBreakTasks.addAll(breakTasks);
            prevClientPlaceTasks.clear();
            prevClientPlaceTasks.addAll(clientPlaceTasks);
            clientPlaceTasks.clear();
            clientPlaceTasks.addAll(placeTasks);
            prevRobotPos = robotPos;
            if (!breakTasks.isEmpty()) {
                Vec3 newRobotPos = breakTasks.stream()
                    .map(breakTask -> breakTask.pos)
                    .map(Vec3::atLowerCornerOf)
                    .map(VecUtil.VEC_HALF::add)
                    .reduce(Vec3.ZERO, Vec3::add)
                    .scale(1D / breakTasks.size());
                newRobotPos = new Vec3(
                    newRobotPos.x,
                    breakTasks.stream()
                        .map(breakTask -> breakTask.pos)
                        .mapToDouble(BlockPos::getY)
                        .max()
                        .orElse(newRobotPos.y),
                    newRobotPos.z
                );
                newRobotPos = newRobotPos.add(new Vec3(0, 3, 0));
                Vec3 oldRobotPos = robotPos;
                robotPos = newRobotPos;
                if (oldRobotPos != null) {
                    robotPos = oldRobotPos.add(newRobotPos.subtract(oldRobotPos).scale(1 / 4D));
                }
            } else {
                robotPos = null;
            }
            return false;
        }

        boolean checkResultsChanged = false;

        tile.getWorldBC().getProfiler().push("scan");
        for (int i = 0; i < CHECKS_PER_TICK; i++) {
            if (check(indexToPos(currentCheckIndex))) {
                checkResultsChanged = true;
            }
            currentCheckIndex = (currentCheckIndex + 1) % checkOrder.length;
        }
        
        int checkIndex0 = (currentCheckIndex + 1) % checkOrder.length;
        boolean isDone = checkResults[checkIndex0] != CHECK_RESULT_UNKNOWN; //FIXED add uncheckResult check
        
        tile.getWorldBC().getProfiler().pop();

        tile.getWorldBC().getProfiler().push("remove tasks");
        tile.getWorldBC().getProfiler().push("break");
        for (Iterator<BreakTask> iterator = breakTasks.iterator(); iterator.hasNext(); ) {
            BreakTask breakTask = iterator.next();
            if (checkResults[posToIndex(breakTask.pos)] == CHECK_RESULT_CORRECT) {
                iterator.remove();
                cancelBreakTask(breakTask);
            }
        }
        tile.getWorldBC().getProfiler().popPush("place");
        for (Iterator<PlaceTask> iterator = placeTasks.iterator(); iterator.hasNext(); ) {
            PlaceTask placeTask = iterator.next();
            if (checkResults[posToIndex(placeTask.pos)] == CHECK_RESULT_CORRECT) {
                iterator.remove();
                cancelPlaceTask(placeTask);
            }
        }
        tile.getWorldBC().getProfiler().pop();
        tile.getWorldBC().getProfiler().pop();

        tile.getWorldBC().getProfiler().push("add tasks");
        tile.getWorldBC().getProfiler().push("break");
        if (tile.canExcavate()) {
            Set<Integer> breakTasksIndexes = breakTasks.stream()
                .map(breakTask -> posToIndex(breakTask.pos))
                .collect(Collectors.toSet());
            int[] blocks = Arrays.stream(breakOrder)
                .filter(i -> checkResults[i] == CHECK_RESULT_TO_BREAK && !breakTasksIndexes.contains(i))
                .toArray();
            leftToBreak = blocks.length;
            if (blocks.length != 0) {
                isDone = false;
            }
            Arrays.stream(blocks)
                .mapToObj(this::indexToPos)
                .filter(blockPos -> BlockUtil.getFluidWithFlowing(tile.getWorldBC(), blockPos) == Fluids.EMPTY)
                .map(blockPos ->
                    new BreakTask(
                        blockPos,
                        0
                    )
                )
                .limit(MAX_QUEUE_SIZE - breakTasks.size())
                .forEach(breakTasks::add);
        } else {
            leftToBreak = 0;
        }
        tile.getWorldBC().getProfiler().pop();
        tile.getWorldBC().getProfiler().push("place");
        {
            Set<Integer> placeTasksIndexes = placeTasks.stream()
                .map(placeTask -> posToIndex(placeTask.pos))
                .collect(Collectors.toSet());
            int[] blocks = Arrays.stream(placeOrder)
                .filter(i -> checkResults[i] == CHECK_RESULT_TO_PLACE && !placeTasksIndexes.contains(i))
                .toArray();
            leftToPlace = blocks.length;
            if (!tile.canExcavate() || breakTasks.isEmpty()) {
                if (blocks.length != 0) {
                    isDone = false;
                }
                Arrays.stream(blocks)
                    .filter(i -> {
                        if (requiredCache[i] != REQUIRED_UNKNOWN) {
                            return requiredCache[i] == REQUIRED_TRUE;
                        }
                        boolean has = hasEnoughToPlaceItems(indexToPos(i));
                        requiredCache[i] = has ? REQUIRED_TRUE : REQUIRED_FALSE;
                        return has;
                    })
                    .mapToObj(this::indexToPos)
                    .filter(this::isReadyToPlace)
                    .limit(MAX_QUEUE_SIZE - placeTasks.size())
                    .filter(this::canPlace)
                    .map(blockPos ->
                        new PlaceTask(
                            blockPos,
                            getToPlaceItems(blockPos),
                            0
                        )
                    )
                    .filter(placeTask -> placeTask.items != null)
                    .forEach(placeTasks::add);
            }
        }
        tile.getWorldBC().getProfiler().pop();
        tile.getWorldBC().getProfiler().pop();

        tile.getWorldBC().getProfiler().push("do tasks");
        long max = Math.min(
            (long) (
                MAX_POWER_PER_TICK *
                    (double) (tile.getBattery().getStored() + MAX_POWER_PER_TICK / 10) /
                    (tile.getBattery().getCapacity() * 2)
            ),
            MAX_POWER_PER_TICK
        );
        tile.getWorldBC().getProfiler().push("break");
        if (!breakTasks.isEmpty()) {
            for (Iterator<BreakTask> iterator = breakTasks.iterator(); iterator.hasNext(); ) {
                BreakTask breakTask = iterator.next();
                if (breakTask.isImpossible()) {
                    continue;
                }
                long target = breakTask.getTarget();
                breakTask.power += tile.getBattery().extractPower(
                    0,
                    Math.min(
                        target - breakTask.power,
                        max / breakTasks.size()
                    )
                );
                if (breakTask.power >= target) {
                    tile.getWorldBC().getProfiler().push("work");
                    tile.getWorldBC().destroyBlockProgress(
                        breakTask.pos.hashCode(),
                        breakTask.pos,
                        -1
                    );
                    Optional<List<ItemStack>> stacks = BlockUtil.breakBlockAndGetDrops(
                        (ServerLevel) tile.getWorldBC(),
                        breakTask.pos,
                        new ItemStack(Items.DIAMOND_PICKAXE),
                        tile.getOwner()
                    );
                    tile.getWorldBC().getProfiler().pop();
                    if (!stacks.isPresent()) {
                        cancelBreakTask(breakTask);
                    }
                    if (check(breakTask.pos)) {
                        checkResultsChanged = true;
                    }
                    iterator.remove();
                } else {
                    tile.getWorldBC().getProfiler().push("work");
                    tile.getWorldBC().destroyBlockProgress(
                        breakTask.pos.hashCode(),
                        breakTask.pos,
                        (int) ((breakTask.power * 9) / target)
                    );
                    tile.getWorldBC().getProfiler().pop();
                }
            }
        }
        tile.getWorldBC().getProfiler().popPush("place");
        if (!placeTasks.isEmpty()) {
            for (Iterator<PlaceTask> iterator = placeTasks.iterator(); iterator.hasNext(); ) {
                PlaceTask placeTask = iterator.next();
                long target = placeTask.getTarget();
                placeTask.power += tile.getBattery().extractPower(
                    0,
                    Math.min(
                        target - placeTask.power,
                        max / placeTasks.size()
                    )
                );
                if (placeTask.power >= target) {
                    tile.getWorldBC().getProfiler().push("work");
                    if (!doPlaceTask(placeTask)) {
                        cancelPlaceTask(placeTask);
                    }
                    tile.getWorldBC().getProfiler().pop();
                    if (check(placeTask.pos)) {
                        checkResultsChanged = true;
                    }
                    iterator.remove();
                }
            }
        }
        tile.getWorldBC().getProfiler().pop();
        tile.getWorldBC().getProfiler().pop();

        if (checkResultsChanged) {
            afterChecks();
        }
//        if(isDone)
        //	BCLog.d(getBuildingInfo().box+" : done");
  //      return isDone;
        boolean isDone0 = true;
        for(int i0 = 0; (i0 < checkResults.length)&&isDone0 ; i0++) {
        	isDone0 = (checkResults[i0] == CHECK_RESULT_CORRECT);
        }
        return isDone0;
    }

   
    protected int posToIndex(BlockPos blockPos) {
        return getBuildingInfo().getSnapshot().posToIndex(getBuildingInfo().fromWorld(blockPos));
    }

   
    protected BlockPos indexToPos(int i) {
        return getBuildingInfo().toWorld(getBuildingInfo().getSnapshot().indexToPos(i));
    }

    /**
     * @return true if changed, false otherwise
     */
    protected boolean check(BlockPos blockPos) {
        int i = posToIndex(blockPos);
        byte prev = checkResults[i];
        if (isAir(blockPos)) {
            if (tile.getWorldBC().isEmptyBlock(blockPos)) {
                checkResults[i] = CHECK_RESULT_CORRECT;
            } else {
                checkResults[i] = CHECK_RESULT_TO_BREAK;
            }
        } else {
            if (isBlockCorrect(blockPos)) {
                checkResults[i] = CHECK_RESULT_CORRECT;
            } else if (canPlace(blockPos)) {
                checkResults[i] = CHECK_RESULT_TO_PLACE;
            } else {
                checkResults[i] = CHECK_RESULT_TO_BREAK;
            }
        }
        return prev != checkResults[i];
    }

    protected void afterChecks() {
    }
    
    public GameEventListener getListener() {
    	return this.worldEventListener;
    }

    public void writeToByteBuf(FriendlyByteBuf buffer) {
        buffer.writeInt(breakTasks.size());
        breakTasks.forEach(breakTask -> breakTask.writePayload(buffer));
        buffer.writeInt(placeTasks.size());
        placeTasks.forEach(placeTask -> placeTask.writePayload(buffer));
        buffer.writeInt(leftToBreak);
        buffer.writeInt(leftToPlace);
    }

    public void readFromByteBuf(FriendlyByteBuf buffer) {
        breakTasks.clear();
        IntStream.range(0, buffer.readInt()).mapToObj(i -> new BreakTask(buffer)).forEach(breakTasks::add);
        placeTasks.clear();
        IntStream.range(0, buffer.readInt()).mapToObj(i -> new PlaceTask(buffer)).forEach(placeTasks::add);
        leftToBreak = buffer.readInt();
        leftToPlace = buffer.readInt();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putByteArray("checkResults", checkResults);
        nbt.put("breakTasks", NBTUtilBC.writeObjectList(breakTasks.stream().map(BreakTask::writeToNBT)));
        nbt.put("placeTasks", NBTUtilBC.writeObjectList(placeTasks.stream().map(PlaceTask::writeToNBT)));
        nbt.putInt("currentCheckIndex", currentCheckIndex);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        updateSnapshot();
        checkResults = nbt.getByteArray("checkResults");
        breakTasks.clear();
        NBTUtilBC.readCompoundList(nbt.get("breakTasks")).map(BreakTask::new).forEach(breakTasks::add);
        placeTasks.clear();
        NBTUtilBC.readCompoundList(nbt.get("placeTasks")).map(PlaceTask::new).forEach(placeTasks::add);
        currentCheckIndex = nbt.getInt("currentCheckIndex");
    }

    public class BreakTask {
        public final BlockPos pos;
        public long power;

       
        public BreakTask(BlockPos pos, long power) {
            this.pos = pos;
            this.power = power;
        }

       
        public BreakTask(FriendlyByteBuf buffer) {
            pos = MessageUtil.readBlockPos(buffer);
            power = buffer.readLong();
        }

       
        public BreakTask(CompoundTag nbt) {
            pos = NbtUtils.readBlockPos(nbt.getCompound("pos"));
            power = nbt.getLong("power");
        }

       
        public boolean isImpossible() {
            return BlockUtil.isUnbreakableBlock(tile.getWorldBC(), pos, tile.getOwner());
        }

        public long getTarget() {
            return BlockUtil.computeBlockBreakPower(tile.getWorldBC(), pos);
        }

        public void writePayload(FriendlyByteBuf buffer) {
            MessageUtil.writeBlockPos(buffer, pos);
            buffer.writeLong(power);
        }

        public CompoundTag writeToNBT() {
            CompoundTag nbt = new CompoundTag();
            nbt.put("pos", NbtUtils.writeBlockPos(pos));
            nbt.putLong("power", power);
            return nbt;
        }
    }

    public class PlaceTask {
        public final BlockPos pos;
        public final List<ItemStack> items;
        public long power;

       
        public PlaceTask(BlockPos pos, List<ItemStack> items, long power) {
            this.pos = pos;
            this.items = Optional.ofNullable(items).map(ImmutableList::copyOf).orElse(null);
            this.power = power;
        }

       
        public PlaceTask(FriendlyByteBuf buffer) {
            pos = MessageUtil.readBlockPos(buffer);
            items = IntStream.range(0, buffer.readInt()).mapToObj(j -> {
                return buffer.readItem();
            }).collect(Collectors.toList());
            power = buffer.readLong();
        }

       
        public PlaceTask(CompoundTag nbt) {
            pos = NbtUtils.readBlockPos(nbt.getCompound("pos"));
            items = ImmutableList.copyOf(
                NBTUtilBC.readCompoundList(nbt.get("items"))
                    .map(ItemStack::of).toList()
            );
            power = nbt.getLong("power");
        }

        public long getTarget() {
            return (long) (Math.sqrt(pos.distSqr(tile.getBuilderPos())) * 10 * MjAPI.MJ);
        }

        public void writePayload(FriendlyByteBuf buffer) {
            MessageUtil.writeBlockPos(buffer, pos);
            buffer.writeInt(items.size());
            items.forEach(buffer::writeItem);
            buffer.writeLong(power);
        }

        public CompoundTag writeToNBT() {
            CompoundTag nbt = new CompoundTag();
            nbt.put("pos", NbtUtils.writeBlockPos(pos));
            nbt.put("items", NBTUtilBC.writeObjectList(items.stream().map(ItemStack::serializeNBT)));
            nbt.putLong("power", power);
            return nbt;
        }
    }
}
