/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.boards;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.core.IZone;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.builders.snapshot.Blueprint;
import buildcraft.builders.snapshot.BlueprintBuilder;
import buildcraft.builders.tile.TileMarkerConstruction;
import buildcraft.lib.inventory.filter.ArrayStackFilter;
import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.misc.VecUtil;
import buildcraft.robotics.ai.*;
import com.google.common.collect.Queues;
import it.unimi.dsi.fastutil.bytes.ByteList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.stream.Collectors;

public class BoardRobotBuilder extends RedstoneBoardRobot {
    private enum EnumRobotBuildStage {
        SEARCH_MARKER,
        DISPOSE_ITEMS,
        CHECK_FOR_BREAK,
        GOTO_BREAK_POS,
        BREAK,
        CHECK_FOR_PLACE,
        GOTO_LOAD,
        GOTO_LOAD_FAILED,
        GOTO_PLACE,
        PLACE,
        SLEEP,
    }

    private EnumRobotBuildStage currentStage = EnumRobotBuildStage.SEARCH_MARKER;

    private static final int MAX_RANGE_SQ = 3 * 64 * 64;

    private TileMarkerConstruction markerToBuild;
    private int launchingDelay = 0;

    // Calen 1.18.2
    private BlueprintBuilder.BreakTask breakTask = null;
    private int bluePrintBuilderLeftToPlace = 0;
    public Blueprint.BuildingInfo currentBuildingInfo = null;
    private Queue<List<ItemStack>> requiredItems = null;
    private List<ItemStack> requiredItemsFor1Block = null;

    public BoardRobotBuilder(EntityRobotBase iRobot) {
        super(iRobot);
    }

    @Override
    public RedstoneBoardRobotNBT getNBTHandler() {
        return BCBoardNBT.REGISTRY.get("builder");
    }

    @Override
    public void update() {
        if (launchingDelay > 0) {
            launchingDelay--;
            return;
        }

        if (!this.hasEnoughEnergy()) {
            startDelegateAI(new AIRobotRecharge(this.robot));
            return;
        }

        // state machine
        flagSwitch:
        switch (currentStage) {
            case SEARCH_MARKER: {
                if (!this.needsToBuild()) {
                    this.resetAll();
                    this.markerToBuild = findClosestMarker();

                    if (markerToBuild == null) {
                        if (robot.containsItems()) {
                            startDelegateAI(new AIRobotDisposeItems(this.robot));
                        } else {
                            startDelegateAI(new AIRobotGotoSleep(this.robot));
                        }
                        return;
                    } else {
                        this.currentBuildingInfo = this.markerToBuild.bptContext;
                        this.markerToBuild.preRobotBuild(this.robot);
                        this.markerToBuild.bluePrintBuilder.updateSnapshot();
                        if (robot.containsItems()) {
                            this.currentStage = EnumRobotBuildStage.DISPOSE_ITEMS;
                            startDelegateAI(new AIRobotDisposeItems(this.robot));
                        } else {
                            startDelegateAI(new AIRobotGotoBlock(robot, markerToBuild.getBlockPos(), 8));
                            this.currentStage = EnumRobotBuildStage.CHECK_FOR_BREAK;
                        }
                    }
                }
                break;
            }
            case DISPOSE_ITEMS: {
                // here AIRobotDisposeItems is finished
                startDelegateAI(new AIRobotGotoBlock(robot, markerToBuild.getBlockPos(), 8));
                this.currentStage = EnumRobotBuildStage.CHECK_FOR_BREAK;
                break;
            }
            case CHECK_FOR_BREAK: {
                if (ByteList.of(markerToBuild.bluePrintBuilder.getCheckResults()).stream().anyMatch(result -> result == BlueprintBuilder.CHECK_RESULT_UNKNOWN)) {
                    this.tickBluePrintBuilder(); // here this.markerToBuild.bluePrintBuilder checks blocks to break
                    break;
                }
                if (markerToBuild.bluePrintBuilder.leftToBreak > 0) {
                    // need to break
                    this.breakTask = markerToBuild.bluePrintBuilder.getBreakTasks().peek();
                    // go to break
                    this.currentStage = EnumRobotBuildStage.GOTO_BREAK_POS;
                    startDelegateAI(new AIRobotGotoBlock(robot, this.breakTask.pos, 8));
                    break;
                } else {
                    this.requiredItems = Queues.newLinkedBlockingQueue(
                            Arrays.asList(markerToBuild.bluePrintBuilder.getRemainingDisplayRequiredBlocks()).stream()
                                    .filter(l -> !l.isEmpty())
                                    .filter(l -> l.size() <= 4)
                                    .map(StackUtil::mergeSameItems)
                                    .collect(Collectors.toList())
                    );
                    this.currentStage = EnumRobotBuildStage.CHECK_FOR_PLACE;
                }
                break;
            }
            case GOTO_BREAK_POS: {
                // here AIRobotGotoBlock is finished
                this.currentStage = EnumRobotBuildStage.BREAK;
                break;
            }
            case BREAK: {
                this.tickBluePrintBuilder();
                if (!this.markerToBuild.bluePrintBuilder.getBreakTasks().contains(this.breakTask)) {
                    // finished breaking
                    this.currentStage = EnumRobotBuildStage.CHECK_FOR_BREAK;
                    this.breakTask = null;
                }
                break;
            }
            case CHECK_FOR_PLACE: {
                if (ByteList.of(markerToBuild.bluePrintBuilder.getCheckResults()).stream().anyMatch(result -> result == BlueprintBuilder.CHECK_RESULT_UNKNOWN)) {
                    this.tickBluePrintBuilder(); // here this.markerToBuild.bluePrintBuilder checks blocks to break
                    break;
                }
                if (!this.requiredItems.isEmpty()) {
                    // need to place
                    this.requiredItemsFor1Block = this.requiredItems.peek();
                    this.currentStage = EnumRobotBuildStage.GOTO_LOAD;
                    startDelegateAI(new AIRobotGotoStationAndLoad(robot, new ArrayStackFilter(requiredItemsFor1Block.get(0)), requiredItemsFor1Block.get(0).getCount()));
                } else {
                    this.currentStage = EnumRobotBuildStage.SLEEP;
                }
                break;
            }
            case GOTO_LOAD: {
                // here AIRobotGotoStationAndLoad is finished
                for (int slotId = 0; slotId < this.requiredItemsFor1Block.size(); slotId++) {
                    ItemStack itemOnRobot = this.robot.getCapability(CapUtil.CAP_ITEMS).orElse(null).getStackInSlot(slotId);
                    if (itemOnRobot.isEmpty()) {
                        startDelegateAI(new AIRobotGotoStationAndLoad(robot, new ArrayStackFilter(requiredItemsFor1Block.get(slotId)), requiredItemsFor1Block.get(slotId).getCount()));
                        break;
                    } else if (!StackUtil.isSameItemSameDamageSameTagSameCount(this.requiredItemsFor1Block.get(slotId), itemOnRobot)) {
                        startDelegateAI(new AIRobotDisposeItems(this.robot));
                        this.currentStage = EnumRobotBuildStage.CHECK_FOR_PLACE;
                        break flagSwitch;
                    }
                }
                // here got all required item for current block
                this.requiredItemsFor1Block = null;
                this.requiredItems.remove();
                this.currentStage = EnumRobotBuildStage.GOTO_PLACE;
                startDelegateAI(new AIRobotGotoBlock(robot, markerToBuild.getBlockPos(), 8));
                break;
            }
            case GOTO_LOAD_FAILED: {
                // here AIRobotGotoStationAndLoad is finished
                this.requiredItemsFor1Block = null;
                this.requiredItems.remove();
                this.currentStage = EnumRobotBuildStage.CHECK_FOR_PLACE;
                startDelegateAI(new AIRobotDisposeItems(this.robot));
                break;
            }
            case GOTO_PLACE: {
                // here AIRobotGotoBlock is finished
                this.markerToBuild.bluePrintBuilder.resourcesChanged(); // force reset check results and avoid finished building 1 block in flowing BlueprintBuilder#tick
                this.tickBluePrintBuilder(); // check blocks to break and place
                if (this.markerToBuild.bluePrintBuilder.leftToBreak > 0) {
                    // the robot has arrived at the marker to place, but found block(s) to break
                    startDelegateAI(new AIRobotDisposeItems(this.robot));
                    break;
                }
                this.bluePrintBuilderLeftToPlace = this.markerToBuild.bluePrintBuilder.leftToPlace;
                this.currentStage = EnumRobotBuildStage.PLACE;
                break;
            }
            case PLACE: {
                if (this.markerToBuild.bluePrintBuilder.leftToPlace == this.bluePrintBuilderLeftToPlace) {
                    // this place task not finished
                    this.tickBluePrintBuilder();
                } else {
                    // this place task finished and removed
                    this.currentStage = EnumRobotBuildStage.CHECK_FOR_PLACE;
                }
                break;
            }
            case SLEEP: {
                this.resetAll();
                this.currentStage = EnumRobotBuildStage.SEARCH_MARKER;
                launchingDelay = 40;
                startDelegateAI(new AIRobotGotoSleep(robot));
                break;
            }
        }
        // TODO: take into account cases where the robot can't reach the destination - go to work on another block
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotRecharge) {
            if (this.currentStage == EnumRobotBuildStage.BREAK) {
                this.currentStage = EnumRobotBuildStage.CHECK_FOR_BREAK;
            } else if (this.currentStage == EnumRobotBuildStage.PLACE) {
                this.currentStage = EnumRobotBuildStage.CHECK_FOR_PLACE;
            }
        } else if (ai instanceof AIRobotGotoStationAndLoad) {
            if (ai.success()) {
                this.currentStage = EnumRobotBuildStage.GOTO_LOAD;
                markerToBuild.bluePrintBuilder.resourcesChanged();
            } else {
                this.currentStage = EnumRobotBuildStage.GOTO_LOAD_FAILED;
            }
        }
    }

    @Override
    public void writeSelfToNBT(CompoundTag nbt) {
        super.writeSelfToNBT(nbt);

        nbt.putInt("launchingDelay", launchingDelay);
    }

    @Override
    public void loadSelfFromNBT(CompoundTag nbt) {
        super.loadSelfFromNBT(nbt);

        launchingDelay = nbt.getInt("launchingDelay");
    }

    private TileMarkerConstruction findClosestMarker() {
        double minDistance = Double.MAX_VALUE;
        TileMarkerConstruction minMarker = null;

        IZone zone = robot.getZoneToWork();

        for (TileMarkerConstruction marker : TileMarkerConstruction.currentMarkers) {
            if (marker.getLevel() != robot.level) {
                continue;
            }
            if (!marker.needsToBuild()) {
                continue;
            }
            if (marker.getRobotUsingThisMarker() != null) {
                continue;
            }
            if (zone != null && !zone.contains(VecUtil.convert(marker.getBlockPos()))) {
                continue;
            }

            double dx = robot.getX() - marker.getBlockPos().getX();
            double dy = robot.getY() - marker.getBlockPos().getY();
            double dz = robot.getZ() - marker.getBlockPos().getZ();
            double distance = dx * dx + dy * dy + dz * dz;

            if (distance < minDistance) {
                minMarker = marker;
                minDistance = distance;
            }
        }

        if (minMarker != null && minDistance < MAX_RANGE_SQ) {
            return minMarker;
        } else {
            return null;
        }
    }

    private boolean hasEnoughEnergy() {
        // return robot.getPower() - currentBuildingSlot.getEnergyRequirement() > EntityRobotBase.SAFETY_POWER;
        if (this.needsToBuild()) {
            long bluePrintBuilderRequired =
                    !this.markerToBuild.bluePrintBuilder.getBreakTasks().isEmpty()
                            ?
                            this.markerToBuild.bluePrintBuilder.getBreakTasks().peek().power
                            :
                            (
                                    !this.markerToBuild.bluePrintBuilder.getPlaceTasks().isEmpty()
                                            ?
                                            this.markerToBuild.bluePrintBuilder.getPlaceTasks().peek().power
                                            :
                                            0
                            );
            return robot.getPower() - bluePrintBuilderRequired > EntityRobotBase.SAFETY_POWER;
        } else {
            return robot.getPower() > EntityRobotBase.SAFETY_POWER;
        }
    }

    // Calen 1.18.2
    private void resetAll() {
        this.breakTask = null;
        this.currentBuildingInfo = null;
        this.requiredItems = null;
        this.bluePrintBuilderLeftToPlace = 0;
        if (this.markerToBuild != null) {
            this.markerToBuild.postRobotBuild(this.robot);
            this.markerToBuild = null;
        }
    }

    private boolean needsToBuild() {
        return this.markerToBuild != null && this.markerToBuild.needsToBuild() && Objects.equals(currentBuildingInfo, markerToBuild.bptContext);
    }

    private void tickBluePrintBuilder() {
        if (this.markerToBuild != null && this.markerToBuild.bluePrintBuilder != null) {
            this.markerToBuild.bluePrintBuilder.tick();
            this.markerToBuild.updateClientBluePrintBuilder();
        }
    }
}
