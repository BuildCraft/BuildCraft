/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders.tile;

import java.io.IOException;
import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;

import buildcraft.api.bpt.SchematicBlock;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.IPathProvider;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.tiles.IDebuggable;

import buildcraft.builders.BCBuildersItems;
import buildcraft.builders.bpt.TickingBlueprintBuilder;
import buildcraft.builders.bpt.TickingBlueprintBuilder.EnumBuilderMessage;
import buildcraft.builders.item.ItemBlueprint.BptStorage;
import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.bpt.Blueprint;
import buildcraft.lib.fluids.Tank;
import buildcraft.lib.fluids.TankManager;
import buildcraft.lib.misc.BoundingBoxUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.PositionUtil;
import buildcraft.lib.misc.data.Box;
import buildcraft.lib.misc.data.EnumAxisOrder;
import buildcraft.lib.net.IPayloadWriter;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBCInventory_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;

public class TileBuilder_Neptune extends TileBCInventory_Neptune implements ITickable, IDebuggable {
    public static final int NET_BOX = 10;
    public static final int NET_PATH = 11;
    public static final int NET_CLEAR = 12;
    public static final int NET_BUILD = 13;
    public static final int NET_ANIM_ITEM = 14;
    public static final int NET_ANIM_BLOCK = 15;
    public static final int NET_ANIM_FLUID = 16;
    public static final int NET_ANIM_POWER = 17;
    public static final int NET_ANIM_STATE = 18;

    public final IItemHandlerModifiable invBlueprint = addInventory("blueprint", 1, EnumAccess.BOTH, EnumPipePart.VALUES);
    public final IItemHandlerModifiable invResources = addInventory("resources", 27, EnumAccess.NONE, EnumPipePart.VALUES);

    private final Tank[] tanks = new Tank[] {//
        new Tank("fluid1", Fluid.BUCKET_VOLUME * 8, this),//
        new Tank("fluid2", Fluid.BUCKET_VOLUME * 8, this),//
        new Tank("fluid3", Fluid.BUCKET_VOLUME * 8, this),//
        new Tank("fluid4", Fluid.BUCKET_VOLUME * 8, this) //
    };

    private final TankManager<Tank> tankManager = new TankManager<>(tanks);

    private final MjBattery battery = new MjBattery(1000 * MjAPI.MJ);

    public final TickingBlueprintBuilder tickingBuilder = new TickingBlueprintBuilder(this::sendMessage, this::getSchematic);
    private BuilderAccessor accessor = null;

    /** Stores the real path - just a few block positions. */
    private ImmutableList<BlockPos> path = null;
    /** Stores the real path plus all possible block positions inbetween. Not saved, regenerated from path. */
    private ImmutableList<BlockPos> pathInterpCache = null;
    private int lastIndex = -1;

    private BlockPos lastBptPos;
    private Box lastBox = null;
    private Blueprint currentBpt = null;
    private int cooldown = 0;

    @Override
    protected void onSlotChange(IItemHandlerModifiable itemHandler, int slot, ItemStack before, ItemStack after) {
        if (itemHandler == invBlueprint) {
            // Update bpt + builder
            if (after == null) {
                // builder.releaseAll();
                // animation.reset();
            }
        }
        super.onSlotChange(itemHandler, slot, before, after);
    }

    @Override
    public void onPlacedBy(EntityLivingBase placer, ItemStack stack) {
        super.onPlacedBy(placer, stack);
        EnumFacing thisFacing = getWorld().getBlockState(getPos()).getValue(BlockBCBase_Neptune.PROP_FACING);
        TileEntity inFront = getWorld().getTileEntity(getPos().offset(thisFacing.getOpposite()));
        if (inFront instanceof IPathProvider) {
            IPathProvider provider = (IPathProvider) inFront;
            ImmutableList<BlockPos> copiedPath = ImmutableList.copyOf(provider.getPath());
            if (copiedPath.size() < 2) {
                setPath(null);
            } else {
                setPath(copiedPath);
                provider.removeFromWorld();
            }
            sendNetworkUpdate(NET_RENDER_DATA);
        }
    }

    @Override
    public void update() {
        battery.tick(getWorld(), getPos());
        if (world.isRemote) {
            tickingBuilder.tick(Side.CLIENT);
            // client stuffs
        } else {
            if (accessor != null) {
                accessor.tick();
            }
            ItemStack bpt = invBlueprint.getStackInSlot(0);
            if (bpt != null && bpt.getItem() == BCBuildersItems.blueprint) {
                if (tickingBuilder.tick(Side.SERVER)) {
                    cooldown--;
                    if (cooldown <= 0) {
                        BptStorage storage = BCBuildersItems.blueprint.createStorage(bpt);
                        currentBpt = new Blueprint(storage.getSaved());
                        EnumFacing thisFacing = getWorld().getBlockState(getPos()).getValue(BlockBCBase_Neptune.PROP_FACING);
                        Rotation rotation = PositionUtil.getRotatedFacing(currentBpt.facing, thisFacing, Axis.Y);
                        currentBpt.rotate(Axis.Y, rotation);

                        boolean immediateRestart = false;

                        if (getPath() == null) {
                            BlockPos bptPos = getPos().add(thisFacing.getOpposite().getDirectionVec());
                            lastBptPos = bptPos;
                            BlockPos start = bptPos.add(currentBpt.offset);
                            BlockPos max = currentBpt.size.add(-1, -1, -1);
                            BlockPos end = start.add(max);
                            lastBox = new Box(start, end);
                            lastIndex = -1;
                        } else if (lastBox == null || lastBptPos == null) {
                            BlockPos bptPos = getPath().get(0);
                            lastBptPos = bptPos;
                            BlockPos start = bptPos.add(currentBpt.offset);
                            BlockPos max = currentBpt.size.add(-1, -1, -1);
                            BlockPos end = start.add(max);
                            lastBox = new Box(start, end);
                            lastIndex = 0;
                            immediateRestart = true;
                        } else {
                            BlockPos toStartAt = null;
                            Box toUse = null;
                            for (int i = lastIndex + 1; i < pathInterpCache.size(); i++) {
                                BlockPos toTest = pathInterpCache.get(i);
                                BlockPos start = toTest.add(currentBpt.offset);
                                BlockPos max = currentBpt.size.add(-1, -1, -1);
                                BlockPos end = start.add(max);
                                Box nBox = new Box(start, end);
                                if (!nBox.getBoundingBox().intersectsWith(lastBox.getBoundingBox())) {
                                    toStartAt = toTest;
                                    toUse = nBox;
                                    lastIndex = i;
                                    break;
                                }
                            }
                            if (toStartAt == null) {
                                // failed to find a position on the path
                                lastBox = null;
                                lastBptPos = null;
                                lastIndex = -1;
                            } else {
                                lastBptPos = toStartAt;
                                lastBox = toUse;
                                immediateRestart = true;
                            }
                        }
                        if (immediateRestart) {
                            cooldown = 30;
                        } else {
                            cooldown = 300;
                        }
                        if (accessor != null) {
                            accessor.releaseAll();
                        }
                        accessor = new BuilderAccessor(this, tickingBuilder);
                        tickingBuilder.reset(lastBox, EnumAxisOrder.XZY.getMaxToMinOrder(), accessor);
                        sendNetworkUpdate(NET_RENDER_DATA);
                    }
                }
            } else {
                tickingBuilder.cancel();
                if (lastBox != null) {
                    lastBox = null;
                    lastBptPos = null;
                    sendNetworkUpdate(NET_RENDER_DATA);
                }
            }
        }
    }

    private void setPath(ImmutableList<BlockPos> path) {
        this.path = path;
        if (path != null) {
            int max = path.size() - 1;
            // Create a list of all the possible block positions on the path that could be used
            ImmutableList.Builder<BlockPos> interp = ImmutableList.builder();
            interp.add(path.get(0));
            for (int i = 1; i <= max; i++) {
                final BlockPos from = path.get(i - 1);
                final BlockPos to = path.get(i);
                interp.addAll(PositionUtil.getAllOnPath(from, to));
            }
            pathInterpCache = interp.build();
        } else {
            pathInterpCache = null;
        }
    }

    private SchematicBlock getSchematic(BlockPos bptPos) {
        return currentBpt.getSchematicAt(bptPos);
    }

    // Networking

    private void sendMessage(EnumBuilderMessage type, IPayloadWriter writer) {
        final int id;
        if (type == EnumBuilderMessage.ANIMATION_BLOCK) id = NET_ANIM_BLOCK;
        else if (type == EnumBuilderMessage.ANIMATION_ITEM) id = NET_ANIM_ITEM;
        else if (type == EnumBuilderMessage.ANIMATION_FLUID) id = NET_ANIM_FLUID;
        else if (type == EnumBuilderMessage.ANIMATION_POWER) id = NET_ANIM_POWER;
        else if (type == EnumBuilderMessage.ANIMATION_STATE) id = NET_ANIM_STATE;
        else if (type == EnumBuilderMessage.BOX) id = NET_BOX;
        else if (type == EnumBuilderMessage.BUILD) id = NET_BUILD;
        else if (type == EnumBuilderMessage.CLEAR) id = NET_CLEAR;
        else throw new IllegalArgumentException("Unknown type " + type);
        createAndSendMessage(id, writer);
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Side side) {
        super.writePayload(id, buffer, side);
        if (side == Side.SERVER) {
            if (id == NET_RENDER_DATA) {
                writePayload(NET_BOX, buffer, side);
                writePayload(NET_PATH, buffer, side);
                writePayload(NET_ANIM_STATE, buffer, side);
            } else if (id == NET_BOX) {
                tickingBuilder.writePayload(EnumBuilderMessage.BOX, buffer, side);
            } else if (id == NET_PATH) {
                if (getPath() == null) {
                    buffer.writeInt(0);
                } else {
                    buffer.writeInt(getPath().size());
                    for (BlockPos p : getPath()) {
                        buffer.writeBlockPos(p);
                    }
                }
            } else if (id == NET_ANIM_STATE) {
                tickingBuilder.writePayload(EnumBuilderMessage.ANIMATION_STATE, buffer, side);
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == Side.CLIENT) {
            if (id == NET_RENDER_DATA) {
                readPayload(NET_BOX, buffer, side, ctx);
                readPayload(NET_PATH, buffer, side, ctx);
                readPayload(NET_ANIM_STATE, buffer, side, ctx);
            } else if (id == NET_BOX) {
                tickingBuilder.readPayload(EnumBuilderMessage.BOX, buffer, side);
            } else if (id == NET_PATH) {
                int count = buffer.readInt();
                if (count <= 0) {
                    setPath(null);
                } else {
                    ImmutableList.Builder<BlockPos> nPath = ImmutableList.builder();
                    for (int i = 0; i < count; i++) {
                        nPath.add(buffer.readBlockPos());
                    }
                    setPath(nPath.build());
                }
            } else if (id == NET_CLEAR || id == NET_BUILD) {
                BlockPos changeAt = buffer.readBlockPos();
                double x = changeAt.getX() + 0.5;
                double y = changeAt.getY() + 0.5;
                double z = changeAt.getZ() + 0.5;
                EnumParticleTypes type = id == NET_CLEAR ? EnumParticleTypes.SMOKE_NORMAL : EnumParticleTypes.CLOUD;
                world.spawnParticle(type, x, y, z, 0, 0, 0);
            }
            // All animation types
            else if (id == NET_ANIM_ITEM) tickingBuilder.readPayload(EnumBuilderMessage.ANIMATION_ITEM, buffer, side);
            else if (id == NET_ANIM_BLOCK) tickingBuilder.readPayload(EnumBuilderMessage.ANIMATION_BLOCK, buffer, side);
            else if (id == NET_ANIM_FLUID) tickingBuilder.readPayload(EnumBuilderMessage.ANIMATION_FLUID, buffer, side);
            else if (id == NET_ANIM_POWER) tickingBuilder.readPayload(EnumBuilderMessage.ANIMATION_POWER, buffer, side);
            else if (id == NET_ANIM_STATE) tickingBuilder.readPayload(EnumBuilderMessage.ANIMATION_STATE, buffer, side);
        }
    }

    // Read-write

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        NBTTagList list = nbt.getTagList("path", Constants.NBT.TAG_INT_ARRAY);
        if (list.hasNoTags()) {
            setPath(null);
        } else {
            ImmutableList.Builder<BlockPos> builder = ImmutableList.builder();
            for (int i = 0; i < list.tagCount(); i++) {
                builder.add(NBTUtilBC.readBlockPos(list.get(i)));
            }
            setPath(builder.build());
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        if (getPath() != null) {
            NBTTagList list = new NBTTagList();
            for (BlockPos p : getPath()) {
                list.appendTag(NBTUtilBC.writeBlockPos(p));
            }
            nbt.setTag("path", list);
        }

        return nbt;
    }

    // Capability

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapUtil.CAP_FLUIDS) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapUtil.CAP_FLUIDS) {
            return (T) tankManager;
        }
        return super.getCapability(capability, facing);
    }

    // Rendering

    public ImmutableList<BlockPos> getPath() {
        return path;
    }

    @SideOnly(Side.CLIENT)
    public Box getBox() {
        return tickingBuilder.box;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasFastRenderer() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return BoundingBoxUtil.makeFrom(getPos(), getBox(), getPath());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return Double.MAX_VALUE;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("");
        left.add("cooldown = " + cooldown);
        left.add("lastBptPos = " + lastBptPos);
        left.add("lastBox = " + lastBox);
        left.add("pathInterpCache = " + (pathInterpCache == null ? "null" : pathInterpCache.size()));
        left.add("lastIndex = " + lastIndex);
    }
}
