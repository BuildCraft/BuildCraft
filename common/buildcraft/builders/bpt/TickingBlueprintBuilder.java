package buildcraft.builders.bpt;

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.bpt.IBuilderAccessor;
import buildcraft.api.bpt.Schematic.EnumPreBuildAction;
import buildcraft.api.bpt.Schematic.PreBuildAction;
import buildcraft.api.bpt.SchematicBlock;
import buildcraft.core.lib.utils.MathUtils;
import buildcraft.lib.bpt.builder.BuilderAnimationManager;
import buildcraft.lib.bpt.builder.BuilderAnimationManager.EnumBuilderAnimMessage;
import buildcraft.lib.bpt.helper.VanillaBlockClearer;
import buildcraft.lib.bpt.task.TaskUsable;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.misc.data.AxisOrder;
import buildcraft.lib.misc.data.Box;
import buildcraft.lib.misc.data.BoxIterator;
import buildcraft.lib.net.command.IPayloadWriter;

public class TickingBlueprintBuilder {
    public enum EnumBuilderMessage {
        ANIMATION_STATE,
        ANIMATION_ITEM,
        ANIMATION_BLOCK,
        ANIMATION_FLUID,
        ANIMATION_POWER,
        BOX,
        BUILD,
        CLEAR;
    }

    @FunctionalInterface
    public interface IBuilderMessageSender {
        void sendBuilderMessage(EnumBuilderMessage type, IPayloadWriter writer);
    }

    @FunctionalInterface
    public interface IBuilderSchematicProvider {
        SchematicBlock getSchematic(BlockPos bptPos);
    }

    public IBuilderAccessor accessor;
    public final IBuilderMessageSender sender;
    public final IBuilderSchematicProvider provider;
    public final BuilderAnimationManager animationManager = new BuilderAnimationManager(this::sendAnimationMessage);
    private final Deque<Pair<TaskUsable, BlockPos>> tasks = new LinkedList<>();
    private final Set<BlockPos> blocksCompleted = new HashSet<>();

    public Box box = null;
    private BoxIterator boxIter = null;
    private BlockPos start;
    private boolean hasFinishedPreBuild = false;
    private boolean hasFinishedFully = false;
    private AxisOrder order = null;

    public TickingBlueprintBuilder(IBuilderMessageSender sender, IBuilderSchematicProvider provider) {
        this.sender = sender;
        this.provider = provider;
    }

    public void writePayload(EnumBuilderMessage type, PacketBuffer buffer, Side side) {
        if (side == Side.SERVER) {
            if (type == EnumBuilderMessage.BOX) {
                if (box == null) {
                    buffer.writeBoolean(false);
                } else {
                    buffer.writeBoolean(true);
                    box.writeData(buffer);
                }
            }
        }
    }

    public void readPayload(EnumBuilderMessage type, PacketBuffer buffer, Side side) {
        if (side == Side.CLIENT) {
            if (type == EnumBuilderMessage.BOX) {
                if (buffer.readBoolean()) {
                    box = new Box();
                    box.readData(buffer);
                } else {
                    box = null;
                }
            } else if (type == EnumBuilderMessage.ANIMATION_STATE) {
                animationManager.receiveMessage(EnumBuilderAnimMessage.STATE, buffer);
            } else if (type == EnumBuilderMessage.ANIMATION_ITEM) {
                animationManager.receiveMessage(EnumBuilderAnimMessage.ITEM, buffer);
            } else if (type == EnumBuilderMessage.ANIMATION_BLOCK) {
                animationManager.receiveMessage(EnumBuilderAnimMessage.BLOCK, buffer);
            } else if (type == EnumBuilderMessage.ANIMATION_FLUID) {
                animationManager.receiveMessage(EnumBuilderAnimMessage.FLUID, buffer);
            } else if (type == EnumBuilderMessage.ANIMATION_POWER) {
                animationManager.receiveMessage(EnumBuilderAnimMessage.POWER, buffer);
            }
        }
    }

    private void sendAnimationMessage(EnumBuilderAnimMessage type, IPayloadWriter writer) {
        if (type == EnumBuilderAnimMessage.STATE) sender.sendBuilderMessage(EnumBuilderMessage.ANIMATION_STATE, writer);
        if (type == EnumBuilderAnimMessage.ITEM) sender.sendBuilderMessage(EnumBuilderMessage.ANIMATION_ITEM, writer);
        if (type == EnumBuilderAnimMessage.BLOCK) sender.sendBuilderMessage(EnumBuilderMessage.ANIMATION_BLOCK, writer);
        if (type == EnumBuilderAnimMessage.FLUID) sender.sendBuilderMessage(EnumBuilderMessage.ANIMATION_FLUID, writer);
        if (type == EnumBuilderAnimMessage.POWER) sender.sendBuilderMessage(EnumBuilderMessage.ANIMATION_POWER, writer);
    }

    public void cancel() {
        box = null;
        boxIter = null;
        start = null;
        order = null;
        hasFinishedPreBuild = false;
        tasks.clear();
        blocksCompleted.clear();
    }

    public void reset(Box nBox, AxisOrder nOrder, IBuilderAccessor accessor) {
        cancel();
        if (nBox != null && accessor != null) {
            this.box = nBox;
            this.order = nOrder;
            this.start = box.min();
            this.accessor = accessor;
            initBoxIter();
        }
    }

    private void initBoxIter() {
        boxIter = new BoxIterator(BlockPos.ORIGIN, box.size().subtract(VecUtil.POS_ONE), order, true);
        order = order.invert(Axis.Y);
    }

    /** @param side
     * @return True if this builder has completely finished, false if not (or its on the client). */
    public boolean tick(Side side) {
        animationManager.tick();
        if (side != Side.SERVER) {
            return true;
        }

        for (int i = 0; i < 10 & i < tasks.size(); i++) {
            Pair<TaskUsable, BlockPos> pair = tasks.removeFirst();
            TaskUsable task = pair.getLeft();
            BlockPos buildAt = pair.getRight();
            if (!task.tick(accessor, buildAt)) {
                tasks.addLast(pair);
            }
        }

        if (boxIter != null) {
            if (!hasFinishedPreBuild) {
                if (boxIter.hasFinished()) {
                    if (tasks.isEmpty()) {
                        initBoxIter();
                        hasFinishedPreBuild = true;
                    }
                } else {
                    int clears = 100;
                    while (clears > 0) {
                        clears -= clearSingle();
                        if (boxIter.hasFinished()) {
                            break;
                        }
                    }
                }
            } else {
                int builds = 100;
                while (builds > 0) {
                    builds -= buildSingle();
                    builds -= 30;
                    if (boxIter.hasFinished()) {
                        break;
                    }
                }

                if (boxIter.hasFinished()) {
                    hasFinishedFully = true;
                    boxIter = null;
                    start = null;
                    blocksCompleted.clear();
                }
            }
        }
        return (boxIter == null || hasFinishedFully) && tasks.isEmpty();
    }

    private int clearSingle() {
        BlockPos next = boxIter.getCurrent();
        boxIter.advance();
        SchematicBlock schematic = provider.getSchematic(next);
        BlockPos buildAt = start.add(next);

        if (accessor.hasPermissionToEdit(buildAt)) {
            PreBuildAction action = schematic.createClearingTask(accessor, buildAt);
            int cost = MathUtils.clamp(action.getTimeCost(), 1, 100);
            if (action.getType() == EnumPreBuildAction.REQUIRE_AIR) {
                action = VanillaBlockClearer.DESTORY_ITEMS;
            }

            TaskUsable clears = action.getTask(accessor, buildAt);
            tasks.add(Pair.of(clears, buildAt));
            sender.sendBuilderMessage(EnumBuilderMessage.CLEAR, (buffer) -> {
                buffer.writeBlockPos(buildAt);
            });
            return cost;
        } else {
            return 1;
        }
    }

    private int buildSingle() {
        BlockPos next = boxIter.getCurrent();
        boxIter.advance();
        SchematicBlock schematic = provider.getSchematic(next);
        BlockPos buildAt = start.add(next);

        if (accessor.hasPermissionToEdit(buildAt)) {
            int cost = MathUtils.clamp(schematic.getTimeCost(), 1, 100);
            TaskUsable task = schematic.createTask(accessor, buildAt);
            tasks.add(Pair.of(task, buildAt));
            sender.sendBuilderMessage(EnumBuilderMessage.BUILD, (buffer) -> {
                buffer.writeBlockPos(buildAt);
            });
            return cost;
        } else {
            return 1;
        }
    }
}
