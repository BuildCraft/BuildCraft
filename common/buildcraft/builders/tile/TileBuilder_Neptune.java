/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders.tile;

import java.io.IOException;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.ITickable;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.items.IItemHandlerModifiable;

import buildcraft.api.bpt.SchematicBlock;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.builders.BCBuildersItems;
import buildcraft.builders.item.ItemBlueprint.BptStorage;
import buildcraft.core.Box;
import buildcraft.core.lib.utils.Utils.EnumAxisOrder;
import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.bpt.Blueprint;
import buildcraft.lib.bpt.builder.BuilderAnimationManager;
import buildcraft.lib.bpt.builder.BuilderAnimationManager.EnumBuilderAnimMessage;
import buildcraft.lib.fluids.Tank;
import buildcraft.lib.fluids.TankManager;
import buildcraft.lib.misc.BoxIterator;
import buildcraft.lib.misc.PositionUtil;
import buildcraft.lib.misc.SoundUtil;
import buildcraft.lib.net.command.IPayloadWriter;
import buildcraft.lib.tile.TileBCInventory_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;

public class TileBuilder_Neptune extends TileBCInventory_Neptune implements ITickable {
    public static final int NET_BOX = 10;
    public static final int NET_PATH = 11;
    public static final int NET_ANIM_ITEM = 12;
    public static final int NET_ANIM_BLOCK = 13;
    public static final int NET_ANIM_FLUID = 14;
    public static final int NET_ANIM_POWER = 15;
    public static final int NET_ANIM_STATE = 16;

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
    private List<BlockPos> path = null;
    private Box box = null;
    private BoxIterator boxIter = null;
    private Blueprint currentBpt = null;
    private Rotation rotation = null;
    private BlockPos start;

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
        battery.tick(getWorld(), getPos());
        builder.update();
        animation.update();
        if (worldObj.isRemote) {
            // client stuffs
        } else {
            // server stuffs
            if (currentBpt != null) {
                BlockPos next = boxIter.getCurrent();
                boxIter.advance();
                SchematicBlock schematic = currentBpt.getSchematicAt(next);
                BlockPos buildAt = start.add(next);

                // Remove the current block
                IBlockState current = getWorld().getBlockState(buildAt);
                if (!current.getBlock().isAir(current, getWorld(), buildAt)) {
                    SoundUtil.playBlockBreak(getWorld(), buildAt, current);
                }
                getWorld().setBlockToAir(buildAt);

                // And build the new one
                schematic.buildImmediatly(getWorld(), builder, buildAt);

                if (boxIter.hasFinished()) {
                    currentBpt = null;
                    boxIter = null;
                    rotation = null;
                    start = null;
                }
            } else {
                cooldown--;
            }

            // TODO: Work out what is happening with rotations and offsets and positions...

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
                    boxIter = new BoxIterator(BlockPos.ORIGIN, max, EnumAxisOrder.XZY.defaultOrder, true);
                    cooldown = 3000;
                }
            }
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

            } else if (id == NET_PATH) {

            } else if (id == NET_ANIM_STATE) {
                animation.writeStatePayload(buffer);
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBuffer buffer, Side side) throws IOException {
        super.readPayload(id, buffer, side);
        if (side == Side.CLIENT) {
            if (id == NET_RENDER_DATA) {
                readPayload(NET_BOX, buffer, side);
                readPayload(NET_PATH, buffer, side);
                readPayload(NET_ANIM_STATE, buffer, side);
            } else if (id == NET_BOX) {

            } else if (id == NET_PATH) {

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
}
