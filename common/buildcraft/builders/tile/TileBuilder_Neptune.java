/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders.tile;

import java.io.IOException;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.items.IItemHandlerModifiable;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.core.Box;
import buildcraft.lib.bpt.builder.BuilderAnimationManager;
import buildcraft.lib.bpt.builder.BuilderAnimationManager.EnumBuilderAnimMessage;
import buildcraft.lib.fluids.Tank;
import buildcraft.lib.fluids.TankManager;
import buildcraft.lib.net.command.IPayloadWriter;
import buildcraft.lib.tile.TileBCInventory_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;

public class TileBuilder_Neptune extends TileBCInventory_Neptune implements ITickable {
    private static int netId = NET_IDS_INV;

    public static final int NET_BOX = netId++;
    public static final int NET_PATH = netId++;
    public static final int NET_ANIM_ITEM = netId++;
    public static final int NET_ANIM_BLOCK = netId++;
    public static final int NET_ANIM_FLUID = netId++;
    public static final int NET_ANIM_POWER = netId++;
    public static final int NET_ANIM_STATE = netId++;

    public static final int NET_IDS_BUILDER = netId;

    public final BuilderAnimationManager animation = new BuilderAnimationManager(this::sendMessage);
    private final IItemHandlerModifiable invBlueprint = addInventory("blueprint", 1, EnumAccess.BOTH, EnumPipePart.VALUES);
    private final IItemHandlerModifiable invResources = addInventory("resources", 28, EnumAccess.INSERT, EnumPipePart.VALUES);

    private final Tank[] tanks = new Tank[] {//
        new Tank("fluid1", Fluid.BUCKET_VOLUME * 8, this),//
        new Tank("fluid2", Fluid.BUCKET_VOLUME * 8, this),//
        new Tank("fluid3", Fluid.BUCKET_VOLUME * 8, this),//
        new Tank("fluid4", Fluid.BUCKET_VOLUME * 8, this) //
    };

    private final TankManager<Tank> tankManager = new TankManager<>(tanks);

    private final MjBattery battery = new MjBattery(1000 * MjAPI.MJ);

    private TileBuilderAccessor builder = null;
    private List<BlockPos> path = null;
    private Box box = null;

    @Override
    protected void onSlotChange(IItemHandlerModifiable itemHandler, int slot, ItemStack before, ItemStack after) {
        if (itemHandler == invBlueprint) {
            // Update bpt + builder
            if (after == null) {
                // builder.releaseAll();
                builder = null;
                animation.reset();
            }
        }
    }

    @Override
    public void update() {
        battery.tick(getWorld(), getPos());
        if (builder != null) {
            builder.update();
            animation.update();
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
