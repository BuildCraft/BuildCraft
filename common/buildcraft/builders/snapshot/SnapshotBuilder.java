/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import java.io.IOException;
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

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import net.minecraftforge.common.util.INBTSerializable;

import buildcraft.api.mj.MjAPI;

import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.world.WorldEventListenerAdapter;

public abstract class SnapshotBuilder<T extends ITileForSnapshotBuilder> implements INBTSerializable<NBTTagCompound> {
    private static final int MAX_QUEUE_SIZE = 16;
    @SuppressWarnings("WeakerAccess")
    protected static final byte CHECK_RESULT_UNKNOWN = 0;
    @SuppressWarnings("WeakerAccess")
    protected static final byte CHECK_RESULT_CORRECT = 1;
    @SuppressWarnings("WeakerAccess")
    protected static final byte CHECK_RESULT_TO_BREAK = 2;
    @SuppressWarnings("WeakerAccess")
    protected static final byte CHECK_RESULT_TO_PLACE = 3;
    private static final byte REQUIRED_UNKNOWN = 0;
    private static final byte REQUIRED_TRUE = 1;
    private static final byte REQUIRED_FALSE = 2;
    private static final int CHECKS_PER_TICK = 10;
    private static final long MAX_POWER_PER_TICK = 256 * MjAPI.MJ;

    protected final T tile;
    private final IWorldEventListener worldEventListener = new WorldEventListenerAdapter() {
        @Override
        public void notifyBlockUpdate(@Nonnull World world,
                                      @Nonnull BlockPos pos,
                                      @Nonnull IBlockState oldState,
                                      @Nonnull IBlockState newState,
                                      int flags) {
            if (tile.getBuilder() == SnapshotBuilder.this && getBuildingInfo() != null && getBuildingInfo().box.contains(pos)) {
                if (check(pos)) {
                    afterChecks();
                }
            }
        }
    };
    private final Queue<BreakTask> breakTasks = new ArrayDeque<>();
    public final Queue<BreakTask> clientBreakTasks = new ArrayDeque<>();
    @SuppressWarnings("WeakerAccess")
    public final Queue<BreakTask> prevClientBreakTasks = new ArrayDeque<>();
    private final Queue<PlaceTask> placeTasks = new ArrayDeque<>();
    public final Queue<PlaceTask> clientPlaceTasks = new ArrayDeque<>();
    @SuppressWarnings("WeakerAccess")
    public final Queue<PlaceTask> prevClientPlaceTasks = new ArrayDeque<>();
    @SuppressWarnings("WeakerAccess")
    protected byte[] checkResults;
    private byte[] requiredCache;
    private int[] breakOrder;
    private int[] placeOrder;
    private int[] checkOrder;
    private int currentCheckIndex;
    public Vec3d robotPos = null;
    public Vec3d prevRobotPos = null;
    public int leftToBreak = 0;
    public int leftToPlace = 0;

    @SuppressWarnings("WeakerAccess")
    protected SnapshotBuilder(T tile) {
        this.tile = tile;
    }

    protected abstract Snapshot.BuildingInfo getBuildingInfo();

    public void validate() {
        if (!tile.getWorldBC().isRemote) {
            tile.getWorldBC().addEventListener(worldEventListener);
        }
    }

    public void invalidate() {
        if (!tile.getWorldBC().isRemote) {
            tile.getWorldBC().removeEventListener(worldEventListener);
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
            false
        );
    }

    /**
     * Executed if {@link #doPlaceTask} failed
     */
    protected void cancelPlaceTask(PlaceTask placeTask) {
        tile.getBattery().addPower(
            Math.min(placeTask.power, tile.getBattery().getCapacity() - tile.getBattery().getStored()),
            false
        );
    }

    /**
     * @return true if block in wold is correct (is not to break) according to snapshot, false otherwise
     */
    protected abstract boolean isBlockCorrect(BlockPos blockPos);

    /**
     * @return Pos where flying item should be rendered
     */
    public Vec3d getPlaceTaskItemPos(PlaceTask placeTask) {
        Vec3d height = new Vec3d(placeTask.pos.subtract(tile.getBuilderPos()));
        double progress = placeTask.power * 1D / placeTask.getTarget();
        return new Vec3d(tile.getBuilderPos())
            .add(height.scale(progress))
            .add(new Vec3d(0, Math.sin(progress * Math.PI) * (Math.abs(height.y) + 1), 0))
            .add(new Vec3d(0.5, 1, 0.5));
    }

    public void updateSnapshot() {
        tile.getWorldBC().profiler.startSection("init");
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
        tile.getWorldBC().profiler.endSection();
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
    public boolean tick() {
        if (tile.getWorldBC().isRemote) {
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
                Vec3d newRobotPos = breakTasks.stream()
                    .map(breakTask -> breakTask.pos)
                    .map(Vec3d::new)
                    .map(VecUtil.VEC_HALF::add)
                    .reduce(Vec3d.ZERO, Vec3d::add)
                    .scale(1D / breakTasks.size());
                newRobotPos = new Vec3d(
                    newRobotPos.x,
                    breakTasks.stream()
                        .map(breakTask -> breakTask.pos)
                        .mapToDouble(BlockPos::getY)
                        .max()
                        .orElse(newRobotPos.y),
                    newRobotPos.z
                );
                newRobotPos = newRobotPos.add(new Vec3d(0, 3, 0));
                Vec3d oldRobotPos = robotPos;
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

        tile.getWorldBC().profiler.startSection("scan");
        for (int i = 0; i < CHECKS_PER_TICK; i++) {
            if (check(indexToPos(currentCheckIndex))) {
                checkResultsChanged = true;
            }
            currentCheckIndex = (currentCheckIndex + 1) % checkOrder.length;
        }
        tile.getWorldBC().profiler.endSection();

        tile.getWorldBC().profiler.startSection("remove tasks");
        tile.getWorldBC().profiler.startSection("break");
        for (Iterator<BreakTask> iterator = breakTasks.iterator(); iterator.hasNext(); ) {
            BreakTask breakTask = iterator.next();
            if (checkResults[posToIndex(breakTask.pos)] == CHECK_RESULT_CORRECT) {
                iterator.remove();
                cancelBreakTask(breakTask);
            }
        }
        tile.getWorldBC().profiler.endSection();
        tile.getWorldBC().profiler.startSection("place");
        for (Iterator<PlaceTask> iterator = placeTasks.iterator(); iterator.hasNext(); ) {
            PlaceTask placeTask = iterator.next();
            if (checkResults[posToIndex(placeTask.pos)] == CHECK_RESULT_CORRECT) {
                iterator.remove();
                cancelPlaceTask(placeTask);
            }
        }
        tile.getWorldBC().profiler.endSection();
        tile.getWorldBC().profiler.endSection();

        boolean isDone = true;

        tile.getWorldBC().profiler.startSection("add tasks");
        tile.getWorldBC().profiler.startSection("break");
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
                .filter(blockPos -> BlockUtil.getFluidWithFlowing(tile.getWorldBC(), blockPos) == null)
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
        tile.getWorldBC().profiler.endSection();
        tile.getWorldBC().profiler.startSection("place");
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
        tile.getWorldBC().profiler.endSection();
        tile.getWorldBC().profiler.endSection();

        tile.getWorldBC().profiler.startSection("do tasks");
        long max = Math.min(
            (long) (
                MAX_POWER_PER_TICK *
                    (double) (tile.getBattery().getStored() + MAX_POWER_PER_TICK / 10) /
                    (tile.getBattery().getCapacity() * 2)
            ),
            MAX_POWER_PER_TICK
        );
        tile.getWorldBC().profiler.startSection("break");
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
                    tile.getWorldBC().profiler.startSection("work");
                    tile.getWorldBC().sendBlockBreakProgress(
                        breakTask.pos.hashCode(),
                        breakTask.pos,
                        -1
                    );
                    Optional<List<ItemStack>> stacks = BlockUtil.breakBlockAndGetDrops(
                        (WorldServer) tile.getWorldBC(),
                        breakTask.pos,
                        new ItemStack(Items.DIAMOND_PICKAXE),
                        tile.getOwner()
                    );
                    tile.getWorldBC().profiler.endSection();
                    if (!stacks.isPresent()) {
                        cancelBreakTask(breakTask);
                    }
                    if (check(breakTask.pos)) {
                        checkResultsChanged = true;
                    }
                    iterator.remove();
                } else {
                    tile.getWorldBC().profiler.startSection("work");
                    tile.getWorldBC().sendBlockBreakProgress(
                        breakTask.pos.hashCode(),
                        breakTask.pos,
                        (int) ((breakTask.power * 9) / target)
                    );
                    tile.getWorldBC().profiler.endSection();
                }
            }
        }
        tile.getWorldBC().profiler.endSection();
        tile.getWorldBC().profiler.startSection("place");
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
                    tile.getWorldBC().profiler.startSection("work");
                    if (!doPlaceTask(placeTask)) {
                        cancelPlaceTask(placeTask);
                    }
                    tile.getWorldBC().profiler.endSection();
                    if (check(placeTask.pos)) {
                        checkResultsChanged = true;
                    }
                    iterator.remove();
                }
            }
        }
        tile.getWorldBC().profiler.endSection();
        tile.getWorldBC().profiler.endSection();

        if (checkResultsChanged) {
            afterChecks();
        }
        return isDone;
    }

    @SuppressWarnings("WeakerAccess")
    protected int posToIndex(BlockPos blockPos) {
        return getBuildingInfo().getSnapshot().posToIndex(getBuildingInfo().fromWorld(blockPos));
    }

    @SuppressWarnings("WeakerAccess")
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
            if (tile.getWorldBC().isAirBlock(blockPos)) {
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

    public void writeToByteBuf(PacketBufferBC buffer) {
        buffer.writeInt(breakTasks.size());
        breakTasks.forEach(breakTask -> breakTask.writePayload(buffer));
        buffer.writeInt(placeTasks.size());
        placeTasks.forEach(placeTask -> placeTask.writePayload(buffer));
        buffer.writeInt(leftToBreak);
        buffer.writeInt(leftToPlace);
    }

    public void readFromByteBuf(PacketBufferBC buffer) {
        breakTasks.clear();
        IntStream.range(0, buffer.readInt()).mapToObj(i -> new BreakTask(buffer)).forEach(breakTasks::add);
        placeTasks.clear();
        IntStream.range(0, buffer.readInt()).mapToObj(i -> new PlaceTask(buffer)).forEach(placeTasks::add);
        leftToBreak = buffer.readInt();
        leftToPlace = buffer.readInt();
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setByteArray("checkResults", checkResults);
        nbt.setTag("breakTasks", NBTUtilBC.writeCompoundList(breakTasks.stream().map(BreakTask::writeToNBT)));
        nbt.setTag("placeTasks", NBTUtilBC.writeCompoundList(placeTasks.stream().map(PlaceTask::writeToNBT)));
        nbt.setInteger("currentCheckIndex", currentCheckIndex);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        updateSnapshot();
        checkResults = nbt.getByteArray("checkResults");
        breakTasks.clear();
        NBTUtilBC.readCompoundList(nbt.getTag("breakTasks")).map(BreakTask::new).forEach(breakTasks::add);
        placeTasks.clear();
        NBTUtilBC.readCompoundList(nbt.getTag("placeTasks")).map(PlaceTask::new).forEach(placeTasks::add);
        currentCheckIndex = nbt.getInteger("currentCheckIndex");
    }

    public class BreakTask {
        public final BlockPos pos;
        public long power;

        @SuppressWarnings("WeakerAccess")
        public BreakTask(BlockPos pos, long power) {
            this.pos = pos;
            this.power = power;
        }

        @SuppressWarnings("WeakerAccess")
        public BreakTask(PacketBufferBC buffer) {
            pos = MessageUtil.readBlockPos(buffer);
            power = buffer.readLong();
        }

        @SuppressWarnings("WeakerAccess")
        public BreakTask(NBTTagCompound nbt) {
            pos = NBTUtil.getPosFromTag(nbt.getCompoundTag("pos"));
            power = nbt.getLong("power");
        }

        @SuppressWarnings("WeakerAccess")
        public boolean isImpossible() {
            return BlockUtil.isUnbreakableBlock(tile.getWorldBC(), pos, tile.getOwner());
        }

        public long getTarget() {
            return BlockUtil.computeBlockBreakPower(tile.getWorldBC(), pos);
        }

        public void writePayload(PacketBufferBC buffer) {
            MessageUtil.writeBlockPos(buffer, pos);
            buffer.writeLong(power);
        }

        public NBTTagCompound writeToNBT() {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setTag("pos", NBTUtil.createPosTag(pos));
            nbt.setLong("power", power);
            return nbt;
        }
    }

    public class PlaceTask {
        public final BlockPos pos;
        public final List<ItemStack> items;
        public long power;

        @SuppressWarnings("WeakerAccess")
        public PlaceTask(BlockPos pos, List<ItemStack> items, long power) {
            this.pos = pos;
            this.items = Optional.ofNullable(items).map(ImmutableList::copyOf).orElse(null);
            this.power = power;
        }

        @SuppressWarnings("WeakerAccess")
        public PlaceTask(PacketBufferBC buffer) {
            pos = MessageUtil.readBlockPos(buffer);
            items = IntStream.range(0, buffer.readInt()).mapToObj(j -> {
                try {
                    return buffer.readItemStack();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList());
            power = buffer.readLong();
        }

        @SuppressWarnings("WeakerAccess")
        public PlaceTask(NBTTagCompound nbt) {
            pos = NBTUtil.getPosFromTag(nbt.getCompoundTag("pos"));
            items = ImmutableList.copyOf(
                NBTUtilBC.readCompoundList(nbt.getTag("items"))
                    .map(ItemStack::new)
                    .collect(Collectors.toList())
            );
            power = nbt.getLong("power");
        }

        public long getTarget() {
            return (long) (Math.sqrt(pos.distanceSq(tile.getBuilderPos())) * 10 * MjAPI.MJ);
        }

        public void writePayload(PacketBufferBC buffer) {
            MessageUtil.writeBlockPos(buffer, pos);
            buffer.writeInt(items.size());
            items.forEach(buffer::writeItemStack);
            buffer.writeLong(power);
        }

        public NBTTagCompound writeToNBT() {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setTag("pos", NBTUtil.createPosTag(pos));
            nbt.setTag("items", NBTUtilBC.writeCompoundList(items.stream().map(ItemStack::serializeNBT)));
            nbt.setLong("power", power);
            return nbt;
        }
    }
}
