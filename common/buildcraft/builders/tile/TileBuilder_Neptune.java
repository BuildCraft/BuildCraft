/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders.tile;

import java.io.IOException;
import java.util.*;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;

import buildcraft.api.bpt.IBptTask;
import buildcraft.api.bpt.Schematic.EnumPreBuildAction;
import buildcraft.api.bpt.Schematic.PreBuildAction;
import buildcraft.api.bpt.SchematicBlock;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.builders.BCBuildersItems;
import buildcraft.builders.item.ItemBlueprint.BptStorage;
import buildcraft.core.Box;
import buildcraft.core.lib.utils.MathUtils;
import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.bpt.Blueprint;
import buildcraft.lib.bpt.builder.BuilderAnimationManager;
import buildcraft.lib.bpt.builder.BuilderAnimationManager.EnumBuilderAnimMessage;
import buildcraft.lib.bpt.helper.VanillaBlockClearer;
import buildcraft.lib.fluids.Tank;
import buildcraft.lib.fluids.TankManager;
import buildcraft.lib.misc.BoundingBoxUtil;
import buildcraft.lib.misc.PositionUtil;
import buildcraft.lib.misc.data.BoxIterator;
import buildcraft.lib.misc.data.EnumAxisOrder;
import buildcraft.lib.net.command.IPayloadWriter;
import buildcraft.lib.tile.TileBCInventory_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;

public class TileBuilder_Neptune extends TileBCInventory_Neptune implements ITickable {
    public static final int NET_BOX = 10;
    public static final int NET_PATH = 11;
    public static final int NET_CLEAR = 12;
    public static final int NET_BUILD = 13;
    public static final int NET_ANIM_ITEM = 14;
    public static final int NET_ANIM_BLOCK = 15;
    public static final int NET_ANIM_FLUID = 16;
    public static final int NET_ANIM_POWER = 17;
    public static final int NET_ANIM_STATE = 18;

    public final BuilderAnimationManager animation = new BuilderAnimationManager(this::sendMessage);
    private final IItemHandlerModifiable invBlueprint = addInventory("blueprint", 1, EnumAccess.BOTH, EnumPipePart.VALUES);
    private final IItemHandlerModifiable invResources = addInventory("resources", 28, EnumAccess.NONE, EnumPipePart.VALUES);

    private final Tank[] tanks = new Tank[] {//
        new Tank("fluid1", Fluid.BUCKET_VOLUME * 8, this),//
        new Tank("fluid2", Fluid.BUCKET_VOLUME * 8, this),//
        new Tank("fluid3", Fluid.BUCKET_VOLUME * 8, this),//
        new Tank("fluid4", Fluid.BUCKET_VOLUME * 8, this) //
    };

    private final TankManager<Tank> tankManager = new TankManager<>(tanks);

    private final MjBattery battery = new MjBattery(1000 * MjAPI.MJ);

    private final BuilderAccessor builder = new BuilderAccessor(this);
    private final Deque<Pair<IBptTask, BlockPos>> tasks = new LinkedList<>();
    private final Set<BlockPos> blocksCompleted = new HashSet<>();
    private final Map<BlockPos, List<IBptTask>> blockTasks = new HashMap<>();

    private List<BlockPos> path = null;
    private Box box = null;
    private BoxIterator boxIter = null;
    private Blueprint currentBpt = null;
    private Rotation rotation = null;
    private BlockPos start;
    private boolean hasFinishedPreBuild = false;

    @Override
    protected void onSlotChange(IItemHandlerModifiable itemHandler, int slot, ItemStack before, ItemStack after) {
        if (itemHandler == invBlueprint) {
            // Update bpt + builder
            if (after == null) {
                // builder.releaseAll();
                // animation.reset();
            }
        }
    }

    private int cooldown = 0;

    @Override
    public void update() {
        if (true) return;// until tasks are done
        
        battery.tick(getWorld(), getPos());
        builder.update();
        animation.update();
        if (worldObj.isRemote) {
            // client stuffs
        } else {
            // server stuffs
            for (int i = 0; i < 10 & i < tasks.size(); i++) {
                Pair<IBptTask, BlockPos> pair = tasks.removeFirst();
                IBptTask task = pair.getLeft();
                BlockPos buildAt = pair.getRight();
                Set<EnumFacing> required = task.getRequiredSolidFaces(builder);
                boolean has = true;
                for (EnumFacing face : required) {
                    BlockPos req = buildAt.offset(face);
                    if (box.contains(req)) {
                        if (!blocksCompleted.contains(req)) {
                            has &= getWorld().isSideSolid(req, face.getOpposite());
                        }
                    } else {// Not in the building box
                        has &= getWorld().isSideSolid(req, face.getOpposite());
                    }
                }
                if (has) {
                    task.receivePower(builder, MjAPI.MJ * 40);
                    if (!task.isDone(builder)) {
                        tasks.addLast(pair);
                    }
                } else {
                    tasks.addLast(pair);
                }
            }

            if (currentBpt != null) {
                if (!hasFinishedPreBuild) {
                    if (boxIter.hasFinished()) {
                        if (tasks.isEmpty()) {
                            boxIter = new BoxIterator(BlockPos.ORIGIN, box.size().add(-1, -1, -1), EnumAxisOrder.XZY.getMinToMaxOrder(), true);
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
                        currentBpt = null;
                        boxIter = null;
                        rotation = null;
                        start = null;
                        blocksCompleted.clear();
                    }
                }
            } else {
                cooldown--;
            }

            if (cooldown <= 0 && currentBpt == null) {
                ItemStack bpt = invBlueprint.getStackInSlot(0);
                if (bpt != null && bpt.getItem() == BCBuildersItems.blueprint) {
                    BptStorage storage = BCBuildersItems.blueprint.createStorage(bpt);
                    currentBpt = new Blueprint(storage.getSaved());
                    EnumFacing thisFacing = getWorld().getBlockState(getPos()).getValue(BlockBCBase_Neptune.PROP_FACING);
                    rotation = PositionUtil.getRotatedFacing(currentBpt.facing, thisFacing, Axis.Y);
                    currentBpt.rotate(Axis.Y, rotation);

                    BlockPos bptPos = getPos().add(thisFacing.getOpposite().getDirectionVec());

                    start = bptPos.add(currentBpt.offset);
                    BlockPos max = currentBpt.size.add(-1, -1, -1);
                    BlockPos end = start.add(max);
                    box = new Box(start, end);
                    boxIter = new BoxIterator(BlockPos.ORIGIN, max, EnumAxisOrder.XZY.getMinToMaxOrder(), true);
                    cooldown = 3000;
                    sendNetworkUpdate(NET_RENDER_DATA);
                }
            }
        }
    }

    private int clearSingle() {
        BlockPos next = boxIter.getCurrent();
        boxIter.advance();
        SchematicBlock schematic = currentBpt.getSchematicAt(next);
        BlockPos buildAt = start.add(next);

        if (canEditOther(buildAt)) {
            PreBuildAction action = schematic.createClearingTask(builder, buildAt);
            int cost = MathUtils.clamp(action.getTimeCost(), 1, 100);
            if (action.getType() == EnumPreBuildAction.REQUIRE_AIR) {
                action = VanillaBlockClearer.INSTANCE;
            }

            Collection<IBptTask> clears = action.getTasks(builder, buildAt);
            for (IBptTask task : clears) {
                tasks.add(Pair.of(task, buildAt));
            }
            createAndSendMessage(false, NET_CLEAR, (buffer) -> {
                buffer.writeBlockPos(buildAt);
            });
            return cost + clears.size() * 4;
        } else {
            return 1;
        }
    }

    private int buildSingle() {
        BlockPos next = boxIter.getCurrent();
        boxIter.advance();
        SchematicBlock schematic = currentBpt.getSchematicAt(next);
        BlockPos buildAt = start.add(next);

        if (canEditOther(buildAt)) {
            int cost = MathUtils.clamp(schematic.getTimeCost(), 1, 100);
            Collection<IBptTask> builds = schematic.createTasks(builder, buildAt);
            for (IBptTask task : builds) {
                tasks.add(Pair.of(task, buildAt));
            }
            createAndSendMessage(false, NET_BUILD, (buffer) -> {
                buffer.writeBlockPos(buildAt);
            });
            return cost + builds.size() * 4;
        } else {
            return 1;
        }
    }

    private void advanceBuilder() {

    }

    // Networking

    private void sendMessage(EnumBuilderAnimMessage type, IPayloadWriter writer) {
        int id;
        if (type == EnumBuilderAnimMessage.BLOCK) id = NET_ANIM_BLOCK;
        else if (type == EnumBuilderAnimMessage.ITEM) id = NET_ANIM_ITEM;
        else if (type == EnumBuilderAnimMessage.FLUID) id = NET_ANIM_FLUID;
        else if (type == EnumBuilderAnimMessage.POWER) id = NET_ANIM_POWER;
        else throw new IllegalArgumentException("Unknown type " + type);
        this.createAndSendMessage(false, id, writer);
    }

    @Override
    public void writePayload(int id, PacketBuffer buffer, Side side) {
        super.writePayload(id, buffer, side);
        if (side == Side.SERVER) {
            if (id == NET_RENDER_DATA) {
                writePayload(NET_BOX, buffer, side);
                writePayload(NET_PATH, buffer, side);
                writePayload(NET_ANIM_STATE, buffer, side);
            } else if (id == NET_BOX) {
                if (box == null) {
                    buffer.writeBoolean(false);
                } else {
                    buffer.writeBoolean(true);
                    box.writeData(buffer);
                }
            } else if (id == NET_PATH) {

            } else if (id == NET_ANIM_STATE) {
                animation.writeStatePayload(buffer);
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBuffer buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == Side.CLIENT) {
            if (id == NET_RENDER_DATA) {
                readPayload(NET_BOX, buffer, side, ctx);
                readPayload(NET_PATH, buffer, side, ctx);
                readPayload(NET_ANIM_STATE, buffer, side, ctx);
            } else if (id == NET_BOX) {
                if (buffer.readBoolean()) {
                    box = new Box();
                    box.readData(buffer);
                } else {
                    box = null;
                }
            } else if (id == NET_PATH) {

            } else if (id == NET_CLEAR || id == NET_BUILD) {
                BlockPos changeAt = buffer.readBlockPos();
                double x = changeAt.getX() + 0.5;
                double y = changeAt.getY() + 0.5;
                double z = changeAt.getZ() + 0.5;
                EnumParticleTypes type = id == NET_CLEAR ? EnumParticleTypes.SMOKE_NORMAL : EnumParticleTypes.CLOUD;
                worldObj.spawnParticle(type, x, y, z, 0, 0, 0);
            }
            // All animation types
            else if (id == NET_ANIM_ITEM) animation.receiveMessage(EnumBuilderAnimMessage.ITEM, buffer);
            else if (id == NET_ANIM_BLOCK) animation.receiveMessage(EnumBuilderAnimMessage.BLOCK, buffer);
            else if (id == NET_ANIM_FLUID) animation.receiveMessage(EnumBuilderAnimMessage.FLUID, buffer);
            else if (id == NET_ANIM_POWER) animation.receiveMessage(EnumBuilderAnimMessage.POWER, buffer);
            else if (id == NET_ANIM_STATE) animation.receiveMessage(EnumBuilderAnimMessage.STATE, buffer);
        }
    }

    // Capability

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return (T) tankManager;
        }
        return super.getCapability(capability, facing);
    }

    // Rendering

    @SideOnly(Side.CLIENT)
    public Box getBox() {
        return box;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasFastRenderer() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return BoundingBoxUtil.makeFrom(getPos(), box);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return Double.MAX_VALUE;
    }
}
