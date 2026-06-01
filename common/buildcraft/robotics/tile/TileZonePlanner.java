/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.tile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Direction;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import buildcraft.api.tiles.IDebuggable;

import buildcraft.lib.delta.DeltaInt;
import buildcraft.lib.delta.DeltaManager.EnumNetworkVisibility;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.net.MessageManager;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;
import buildcraft.lib.tile.item.ItemHandlerSimple;

import buildcraft.core.BCCoreItems;
import buildcraft.core.item.ItemMapLocation;
import buildcraft.core.item.ItemPaintbrush_BC8;
import buildcraft.robotics.zone.ZonePlan;
import buildcraft.robotics.zone.ZonePlannerMapChunkKey;
import buildcraft.lib.tile.TileBC_Neptune.NetSide;

public class TileZonePlanner extends TileBC_Neptune implements ITickable, IDebuggable {
    protected static final IdAllocator IDS = TileBC_Neptune.IDS.makeChild("zone_planner");
    public static final int NET_PLAN_CHANGE = IDS.allocId("PLAN_CHANGE");

    public final ItemHandlerSimple invPaintbrushes = itemManager.addInvHandler(
        "paintbrushes",
        16,
        (slot, stack) -> stack.getItem() instanceof ItemPaintbrush_BC8,
        EnumAccess.NONE
    );
    public final ItemHandlerSimple invInputPaintbrush = itemManager.addInvHandler(
        "inputPaintbrush",
        1,
        (slot, stack) -> stack.getItem() instanceof ItemPaintbrush_BC8,
        EnumAccess.NONE
    );
    public final ItemHandlerSimple invInputMapLocation = itemManager.addInvHandler(
        "inputMapLocation",
        1,
        (slot, stack) -> stack.getItem() instanceof ItemMapLocation &&
            Optional.ofNullable(stack.getNbt())
                .map(tagCompound -> tagCompound.contains("chunkMapping"))
                .orElse(false) &&
            stack.getCount() == 1,
        EnumAccess.NONE
    );
    public final ItemHandlerSimple invInputResult = itemManager.addInvHandler(
        "inputResult",
        1,
        EnumAccess.NONE
    );
    public final ItemHandlerSimple invOutputPaintbrush = itemManager.addInvHandler(
        "outputPaintbrush",
        1,
        (slot, stack) -> stack.getItem() instanceof ItemPaintbrush_BC8,
        EnumAccess.NONE
    );
    public final ItemHandlerSimple invOutputMapLocation = itemManager.addInvHandler(
        "outputMapLocation",
        1,
        (slot, stack) -> stack.getItem() instanceof ItemMapLocation && stack.getCount() == 1,
        EnumAccess.NONE
    );
    public final ItemHandlerSimple invOutputResult = itemManager.addInvHandler(
        "outputResult",
        1,
        EnumAccess.NONE
    );
    private int progressInput = 0;
    public final DeltaInt deltaProgressInput = deltaManager.addDelta("progressInput", EnumNetworkVisibility.GUI_ONLY);
    private int progressOutput = 0;
    public final DeltaInt deltaProgressOutput = deltaManager.addDelta("progressOutput", EnumNetworkVisibility.GUI_ONLY);
    public ZonePlan[] layers = new ZonePlan[16];

    public TileZonePlanner() {
        for (int i = 0; i < layers.length; i++) {
            layers[i] = new ZonePlan();
        }
    }

    @Environment(EnvType.CLIENT)
    public int getLevel() {
        BlockPos blockPos = MinecraftClient.getInstance().player.getPosition();
        while (!MinecraftClient.getInstance().world.getBlockState(blockPos).isSideSolid(MinecraftClient.getInstance().world, blockPos, Direction.DOWN) && blockPos.getY() < 255) {
            blockPos = new BlockPos(blockPos.getX(), blockPos.getY() + 1, blockPos.getZ());
        }
        return (int) Math.floor((double) blockPos.getY() / ZonePlannerMapChunkKey.LEVEL_HEIGHT);
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, NetSide side) {
        super.writePayload(id, buffer, side);
        if (side == NetSide.SERVER) {
            if (id == NET_RENDER_DATA) {
                for (ZonePlan layer : layers) {
                    layer.writeToByteBuf(buffer);
                }
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, NetSide side, Object ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == NetSide.CLIENT) {
            if (id == NET_RENDER_DATA) {
                for (int i = 0; i < layers.length; i++) {
                    ZonePlan layer = layers[i];
                    layers[i] = layer.readFromByteBuf(buffer);
                }
            }
        } else if (side == NetSide.SERVER) {
            if (id == NET_PLAN_CHANGE) {
                int index = buffer.readUnsignedShort();
                layers[index].readFromByteBuf(buffer);
                markDirty();
                sendNetworkUpdate(NET_RENDER_DATA);
            }
        }
    }

    @Override
    public NbtCompound writeToNBT(NbtCompound nbt) {
        super.writeToNBT(nbt);
        for (int i = 0; i < layers.length; i++) {
            ZonePlan layer = layers[i];
            NbtCompound layerCompound = new NbtCompound();
            layer.writeToNBT(layerCompound);
            nbt.put("layer_" + i, layerCompound);
        }
        return nbt;
    }

    @Override
    public void readFromNBT(NbtCompound nbt) {
        super.readFromNBT(nbt);
        for (int i = 0; i < layers.length; i++) {
            ZonePlan layer = layers[i];
            layer.readFromNBT(nbt.getCompound("layer_" + i));
        }
    }

    public void sendLayerToServer(int index) {
        IMessage message = createMessage(NET_PLAN_CHANGE, (buffer) -> {
            buffer.writeShort(index);
            layers[index].writeToByteBuf(buffer);
        });
        MessageManager.sendToServer(message);
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        left.add("progress_input = " + progressInput);
        left.add("progress_output = " + progressOutput);
    }

    @Override
    public void update() {
        deltaManager.tick();
        if (getWorld().isClient) {
            return;
        }

        {
            // noinspection ConstantConditions
            if (!invInputPaintbrush.getStackInSlot(0).isEmpty() && invInputPaintbrush.getStackInSlot(0).getItem() instanceof ItemPaintbrush_BC8 && !invInputMapLocation.getStackInSlot(0).isEmpty()
                && invInputMapLocation.getStackInSlot(0).getItem() instanceof ItemMapLocation && invInputMapLocation.getStackInSlot(0).getNbt() != null && invInputMapLocation.getStackInSlot(0)
                    .getNbt().contains("chunkMapping") && invInputResult.getStackInSlot(0).isEmpty()) {
                if (progressInput == 0) {
                    deltaProgressInput.addDelta(0, 200, 1);
                    deltaProgressInput.addDelta(200, 205, -1);
                }

                if (progressInput < 200) {
                    progressInput++;
                    return;
                }

                ZonePlan zonePlan = new ZonePlan();
                zonePlan.readFromNBT(invInputMapLocation.getStackInSlot(0).getNbt());
                layers[BCCoreItems.paintbrush.getBrushFromStack(invInputPaintbrush.getStackInSlot(0)).colour.getId()] = zonePlan.getWithOffset(-pos.getX(), -pos.getZ());
                invInputMapLocation.setStackInSlot(0, StackUtil.EMPTY);
                invInputResult.setStackInSlot(0, new ItemStack(BCCoreItems.mapLocation));
                this.markDirty();
                this.sendNetworkUpdate(NET_RENDER_DATA);
                progressInput = 0;
            } else if (progressInput != -1) {
                progressInput = -1;
                deltaProgressInput.setValue(0);
            }
        }
        {
            if (!invOutputPaintbrush.getStackInSlot(0).isEmpty() && invOutputPaintbrush.getStackInSlot(0).getItem() instanceof ItemPaintbrush_BC8 && !invOutputMapLocation.getStackInSlot(0).isEmpty()
                && invOutputMapLocation.getStackInSlot(0).getItem() instanceof ItemMapLocation && invOutputResult.getStackInSlot(0).isEmpty()) {
                if (progressOutput == 0) {
                    deltaProgressOutput.addDelta(0, 200, 1);
                    deltaProgressOutput.addDelta(200, 205, -1);
                }

                if (progressOutput < 200) {
                    progressOutput++;
                    return;
                }

                ItemMapLocation.setZone(invOutputMapLocation.getStackInSlot(0), layers[BCCoreItems.paintbrush.getBrushFromStack(invOutputPaintbrush.getStackInSlot(0)).colour.getId()]
                    .getWithOffset(pos.getX(), pos.getZ()));
                invOutputResult.setStackInSlot(0, invOutputMapLocation.getStackInSlot(0));
                invOutputMapLocation.setStackInSlot(0, StackUtil.EMPTY);
                progressOutput = 0;
            } else if (progressOutput != -1) {
                progressOutput = -1;
                deltaProgressOutput.setValue(0);
            }
        }
    }
}
