/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;

import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.mj.MjAPI;

import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.data.Box;
import buildcraft.lib.net.PacketBufferBC;

public abstract class SnapshotBuilder<T extends ITileForSnapshotBuilder> {
    private static final int MAX_QUEUE_SIZE = 64;

    protected final T tile;
    private final IWorldEventListener worldEventListener = new IWorldEventListener() {
        @Override
        public void notifyBlockUpdate(World world,
                                      BlockPos pos,
                                      IBlockState oldState,
                                      IBlockState newState,
                                      int flags) {
            if (tile.getBuilder() == SnapshotBuilder.this) {
                check(pos);
            }
        }

        @Override
        public void notifyLightSet(BlockPos pos) {
        }

        @Override
        public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
        }

        @Override
        public void playSoundToAllNearExcept(@Nullable EntityPlayer player,
                                             SoundEvent soundIn,
                                             SoundCategory category,
                                             double x,
                                             double y,
                                             double z,
                                             float volume,
                                             float pitch) {
        }

        @Override
        public void playRecord(SoundEvent soundIn, BlockPos pos) {
        }

        @Override
        public void spawnParticle(int particleID,
                                  boolean ignoreRange,
                                  double xCoord,
                                  double yCoord,
                                  double zCoord,
                                  double xSpeed,
                                  double ySpeed,
                                  double zSpeed,
                                  int... parameters) {
        }

        @Override
        public void spawnParticle(int id,
                                  boolean ignoreRange,
                                  boolean p_190570_3_,
                                  double x,
                                  double y,
                                  double z,
                                  double xSpeed,
                                  double ySpeed,
                                  double zSpeed,
                                  int... parameters) {
        }

        @Override
        public void onEntityAdded(Entity entityIn) {
        }

        @Override
        public void onEntityRemoved(Entity entityIn) {
        }

        @Override
        public void broadcastSound(int soundID, BlockPos pos, int data) {
        }

        @Override
        public void playEvent(EntityPlayer player, int type, BlockPos blockPosIn, int data) {
        }

        @Override
        public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {
        }
    };
    private final Queue<BreakTask> breakTasks = new ArrayDeque<>();
    public final Queue<BreakTask> clientBreakTasks = new ArrayDeque<>();
    public final Queue<BreakTask> prevClientBreakTasks = new ArrayDeque<>();
    private final Queue<PlaceTask> placeTasks = new ArrayDeque<>();
    public final Queue<PlaceTask> clientPlaceTasks = new ArrayDeque<>();
    public final Queue<PlaceTask> prevClientPlaceTasks = new ArrayDeque<>();
    private final LinkedList<BlockPos> toCheck = new LinkedList<>();
    protected final Map<CheckResult, Set<BlockPos>> checkResults;
    public Vec3d robotPos = null;
    public Vec3d prevRobotPos = null;
    public int leftToBreak = 0;
    public int leftToPlace = 0;

    protected SnapshotBuilder(T tile) {
        this.tile = tile;
        checkResults =
            new ImmutableMap.Builder<CheckResult, Set<BlockPos>>()
                .put(
                    CheckResult.CORRECT,
                    new TreeSet<>(BlockUtil.uniqueBlockPosComparator(Comparator.comparingDouble(blockPos ->
                        Math.pow(blockPos.getX() - getBox().center().getX(), 2) +
                            Math.pow(blockPos.getY() - getBox().center().getY(), 2) +
                            Math.pow(blockPos.getZ() - getBox().center().getZ(), 2)
                    )))
                )
                .put(
                    CheckResult.TO_BREAK,
                    new TreeSet<>(BlockUtil.uniqueBlockPosComparator(Comparator.comparingDouble(blockPos ->
                        Math.pow(blockPos.getX() - getBox().center().getX(), 2) +
                            Math.pow(blockPos.getZ() - getBox().center().getZ(), 2) +
                            100_000 - Math.abs(blockPos.getY() - tile.getBuilderPos().getY()) * 100_000
                    )))
                )
                .put(
                    CheckResult.TO_PLACE,
                    new TreeSet<>(BlockUtil.uniqueBlockPosComparator(Comparator.comparingDouble(blockPos ->
                        100_000 - (Math.pow(blockPos.getX() - tile.getBuilderPos().getX(), 2) +
                            Math.pow(blockPos.getZ() - tile.getBuilderPos().getZ(), 2)) +
                            Math.abs(blockPos.getY() - tile.getBuilderPos().getY()) * 100_000
                    )))
                )
                .build();
    }

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

    protected abstract Set<BlockPos> getToBreak();

    protected abstract Set<BlockPos> getToPlace();

    protected abstract boolean canPlace(BlockPos blockPos);

    /**
     * @return items
     */
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

    public abstract Box getBox();

    /**
     * @return Pos where flying item should be rendered
     */
    public Vec3d getPlaceTaskItemPos(PlaceTask placeTask) {
        Vec3d height = new Vec3d(placeTask.pos.subtract(tile.getBuilderPos()));
        double progress = placeTask.power * 1D / placeTask.getTarget();
        return new Vec3d(tile.getBuilderPos())
            .add(height.scale(progress))
            .add(new Vec3d(0, Math.sin(progress * Math.PI) * (Math.abs(height.yCoord) + 1), 0))
            .add(new Vec3d(0.5, 1, 0.5));
    }

    public void updateSnapshot() {
        tile.getWorldBC().profiler.startSection("init");
        toCheck.addAll(getToBreak());
        toCheck.addAll(getToPlace());
        toCheck.sort(BlockUtil.uniqueBlockPosComparator(Comparator.comparingDouble(blockPos ->
            Math.pow(blockPos.getX() - getBox().center().getX(), 2) +
                Math.pow(blockPos.getY() - getBox().center().getY(), 2) +
                Math.pow(blockPos.getZ() - getBox().center().getZ(), 2)
        )));
        tile.getWorldBC().profiler.endSection();
    }

    public void cancel() {
        breakTasks.forEach(breakTask ->
            tile.getBattery().addPower(
                Math.min(breakTask.getTarget(), tile.getBattery().getCapacity() - tile.getBattery().getStored()),
                false
            )
        );
        placeTasks.forEach(placeTask ->
            tile.getBattery().addPower(
                Math.min(placeTask.getTarget(), tile.getBattery().getCapacity() - tile.getBattery().getStored()),
                false
            )
        );
        breakTasks.clear();
        clientBreakTasks.clear();
        prevClientBreakTasks.clear();
        placeTasks.clear();
        clientPlaceTasks.clear();
        prevClientPlaceTasks.clear();
        toCheck.clear();
        checkResults.values().forEach(Collection::clear);
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
                    .reduce(Vec3d.ZERO, Vec3d::add)
                    .scale(1D / breakTasks.size());
                newRobotPos = new Vec3d(
                    newRobotPos.xCoord,
                    breakTasks.stream()
                        .map(breakTask -> breakTask.pos)
                        .mapToDouble(BlockPos::getY)
                        .max()
                        .orElse(newRobotPos.yCoord),
                    newRobotPos.zCoord
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

        tile.getWorldBC().profiler.startSection("scan");
        for (int i = 0; i < 10; i++) {
            BlockPos blockPos = toCheck.pollFirst();
            check(blockPos);
            toCheck.addLast(blockPos);
        }
        tile.getWorldBC().profiler.endSection();

        tile.getWorldBC().profiler.startSection("remove tasks");
        for (Iterator<BreakTask> iterator = breakTasks.iterator(); iterator.hasNext(); ) {
            BreakTask breakTask = iterator.next();
            if (checkResults.get(CheckResult.CORRECT).contains(breakTask.pos)) {
                iterator.remove();
                cancelBreakTask(breakTask);
            }
        }
        for (Iterator<PlaceTask> iterator = placeTasks.iterator(); iterator.hasNext(); ) {
            PlaceTask placeTask = iterator.next();
            if (checkResults.get(CheckResult.CORRECT).contains(placeTask.pos)) {
                iterator.remove();
                cancelPlaceTask(placeTask);
            }
        }
        tile.getWorldBC().profiler.endSection();

        boolean isDone = true;

        tile.getWorldBC().profiler.startSection("add tasks");
        if (tile.canExcavate()) {
            List<BlockPos> blocks = checkResults.get(CheckResult.TO_BREAK).stream()
                .filter(blockPos ->
                    breakTasks.stream()
                        .map(breakTask -> breakTask.pos)
                        .noneMatch(Predicate.isEqual(blockPos))
                )
                .filter(blockPos -> BlockUtil.getFluidWithFlowing(tile.getWorldBC(), blockPos) == null)
                .collect(Collectors.toList());
            leftToBreak = blocks.size();
            if (!blocks.isEmpty()) {
                isDone = false;
            }
            blocks.stream()
                .map(blockPos ->
                    new BreakTask(
                        blockPos,
                        0
                    )
                )
                .limit(MAX_QUEUE_SIZE - breakTasks.size())
                .forEach(breakTasks::add);
        }
        {
            List<BlockPos> blocks = checkResults.get(CheckResult.TO_PLACE).stream()
                .filter(blockPos ->
                    placeTasks.stream()
                        .map(placeTask -> placeTask.pos)
                        .noneMatch(Predicate.isEqual(blockPos))
                )
                .filter(this::canPlace)
                .collect(Collectors.toList());
            leftToPlace = blocks.size();
            if ((!tile.canExcavate() || breakTasks.isEmpty())) {
                if (!blocks.isEmpty()) {
                    isDone = false;
                }
                blocks.stream()
                    .map(blockPos ->
                        new PlaceTask(
                            blockPos,
                            getToPlaceItems(blockPos),
                            0
                        )
                    )
                    .filter(placeTask -> placeTask.items != null)
                    .filter(placeTask -> !placeTask.items.contains(ItemStack.EMPTY))
                    .limit(MAX_QUEUE_SIZE - placeTasks.size())
                    .forEach(placeTasks::add);
            }
        }
        tile.getWorldBC().profiler.endSection();

        tile.getWorldBC().profiler.startSection("do tasks");
        if (!breakTasks.isEmpty()) {
            for (Iterator<BreakTask> iterator = breakTasks.iterator(); iterator.hasNext(); ) {
                BreakTask breakTask = iterator.next();
                long target = breakTask.getTarget();
                breakTask.power += tile.getBattery().extractPower(
                    0,
                    Math.min(
                        Math.min(
                            target - breakTask.power,
                            tile.getBattery().getStored() / breakTasks.size()
                        ),
                        10 * MjAPI.MJ
                    )
                );
                if (breakTask.power >= target) {
                    BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(
                        tile.getWorldBC(),
                        breakTask.pos,
                        tile.getWorldBC().getBlockState(breakTask.pos),
                        BuildCraftAPI.fakePlayerProvider.getFakePlayer(
                            (WorldServer) tile.getWorldBC(),
                            tile.getOwner(),
                            tile.getBuilderPos()
                        )
                    );
                    MinecraftForge.EVENT_BUS.post(breakEvent);
                    if (!breakEvent.isCanceled()) {
                        tile.getWorldBC().sendBlockBreakProgress(
                            breakTask.pos.hashCode(),
                            breakTask.pos,
                            -1
                        );
                        tile.getWorldBC().destroyBlock(breakTask.pos, false);
                    } else {
                        cancelBreakTask(breakTask);
                    }
                    check(breakTask.pos);
                    iterator.remove();
                } else {
                    tile.getWorldBC().sendBlockBreakProgress(
                        breakTask.pos.hashCode(),
                        breakTask.pos,
                        (int) ((breakTask.power * 9) / target)
                    );
                }
            }
        }
        if (!placeTasks.isEmpty()) {
            for (Iterator<PlaceTask> iterator = placeTasks.iterator(); iterator.hasNext(); ) {
                PlaceTask placeTask = iterator.next();
                long target = placeTask.getTarget();
                placeTask.power += tile.getBattery().extractPower(
                    0,
                    Math.min(
                        Math.min(
                            target - placeTask.power,
                            tile.getBattery().getStored() / placeTasks.size()
                        ),
                        10 * MjAPI.MJ
                    )
                );
                if (placeTask.power >= target) {
                    if (!doPlaceTask(placeTask)) {
                        cancelPlaceTask(placeTask);
                    }
                    check(placeTask.pos);
                    iterator.remove();
                }
            }
        }
        tile.getWorldBC().profiler.endSection();

        return isDone;
    }

    protected void check(BlockPos blockPos) {
        checkResults.values().forEach(set -> set.remove(blockPos));
        if (getToBreak().contains(blockPos)) {
            if (tile.getWorldBC().isAirBlock(blockPos)) {
                checkResults.get(CheckResult.CORRECT).add(blockPos);
            } else {
                checkResults.get(CheckResult.TO_BREAK).add(blockPos);
            }
        }
        if (getToPlace().contains(blockPos)) {
            if (isBlockCorrect(blockPos)) {
                checkResults.get(CheckResult.CORRECT).add(blockPos);
            } else if (tile.getWorldBC().isAirBlock(blockPos)) {
                checkResults.get(CheckResult.TO_PLACE).add(blockPos);
            } else {
                checkResults.get(CheckResult.TO_BREAK).add(blockPos);
            }
        }
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

    public class BreakTask {
        public BlockPos pos;
        public long power;

        public BreakTask(BlockPos pos, long power) {
            this.pos = pos;
            this.power = power;
        }

        public BreakTask(PacketBufferBC buffer) {
            pos = MessageUtil.readBlockPos(buffer);
            power = buffer.readLong();
        }

        public long getTarget() {
            return BlockUtil.computeBlockBreakPower(tile.getWorldBC(), pos);
        }

        public void writePayload(PacketBufferBC buffer) {
            MessageUtil.writeBlockPos(buffer, pos);
            buffer.writeLong(power);
        }
    }

    public class PlaceTask {
        public BlockPos pos;
        public List<ItemStack> items;
        public long power;

        public PlaceTask(BlockPos pos, List<ItemStack> items, long power) {
            this.pos = pos;
            this.items = items;
            this.power = power;
        }

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

        public long getTarget() {
            return (long) (Math.sqrt(pos.distanceSq(tile.getBuilderPos())) * 10 * MjAPI.MJ);
        }

        public void writePayload(PacketBufferBC buffer) {
            MessageUtil.writeBlockPos(buffer, pos);
            buffer.writeInt(items.size());
            items.forEach(buffer::writeItemStack);
            buffer.writeLong(power);
        }
    }

    protected enum CheckResult {
        CORRECT,
        TO_BREAK,
        TO_PLACE
    }
}
