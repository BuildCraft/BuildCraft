/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.tile;

import buildcraft.api.net.IMessage;
import buildcraft.api.tiles.IBCTileMenuProvider;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.tiles.ITickable;
import buildcraft.core.BCCoreItems;
import buildcraft.core.item.ItemMapLocation;
import buildcraft.core.item.ItemPaintbrush_BC8;
import buildcraft.lib.delta.DeltaInt;
import buildcraft.lib.delta.DeltaManager.EnumNetworkVisibility;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.net.MessageManager;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import buildcraft.robotics.BCRoboticsBlocks;
import buildcraft.robotics.BCRoboticsMenuTypes;
import buildcraft.robotics.container.ContainerZonePlanner;
import buildcraft.robotics.zone.ZonePlan;
import buildcraft.robotics.zone.ZonePlannerMapChunkKey;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class TileZonePlanner extends TileBC_Neptune implements ITickable, IDebuggable, IBCTileMenuProvider {
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
//                    Optional.ofNullable(stack.getTagCompound())
                    Optional.ofNullable(stack.getTag())
//                            .map(tagCompound -> tagCompound.hasKey("chunkMapping"))
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

    public TileZonePlanner(BlockPos pos, BlockState blockState) {
        super(BCRoboticsBlocks.zonePlannerTile.get(), pos, blockState);
        for (int i = 0; i < layers.length; i++) {
            layers[i] = new ZonePlan();
        }
    }

    @OnlyIn(Dist.CLIENT)
//    public int getLevel()
    public int getLevelBC() {
        BlockPos blockPos = Minecraft.getInstance().player.getOnPos();
//        while (!Minecraft.getMinecraft().world.getBlockState(blockPos).isSideSolid(Minecraft.getMinecraft().world, blockPos, EnumFacing.DOWN) && blockPos.getY() < 255)
        while (!Minecraft.getInstance().level.getBlockState(blockPos).isFaceSturdy(Minecraft.getInstance().level, blockPos, Direction.DOWN) && blockPos.getY() < 255) {
            blockPos = new BlockPos(blockPos.getX(), blockPos.getY() + 1, blockPos.getZ());
        }
        return (int) Math.floor((double) blockPos.getY() / ZonePlannerMapChunkKey.LEVEL_HEIGHT);
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Dist side) {
        super.writePayload(id, buffer, side);
        if (side == Dist.DEDICATED_SERVER) {
            if (id == NET_RENDER_DATA) {
                for (ZonePlan layer : layers) {
                    layer.writeToByteBuf(buffer);
                }
            }
        }
    }

    @Override
//    public void readPayload(int id, PacketBufferBC buffer, Dist side, MessageContext ctx) throws IOException
    public void readPayload(int id, PacketBufferBC buffer, NetworkDirection side, NetworkEvent.Context ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == NetworkDirection.PLAY_TO_CLIENT) {
            if (id == NET_RENDER_DATA) {
                for (int i = 0; i < layers.length; i++) {
                    ZonePlan layer = layers[i];
                    layers[i] = layer.readFromByteBuf(buffer);
                }
            }
        } else if (side == NetworkDirection.PLAY_TO_SERVER) {
            if (id == NET_PLAN_CHANGE) {
                int index = buffer.readUnsignedShort();
                layers[index].readFromByteBuf(buffer);
//                markDirty();
                setChanged();
                sendNetworkUpdate(NET_RENDER_DATA);
            }
        }
    }

    @Override
//    public CompoundTag writeToNBT(CompoundTag nbt)
    public void saveAdditional(CompoundTag nbt) {
//        super.writeToNBT(nbt);
        super.saveAdditional(nbt);
        for (int i = 0; i < layers.length; i++) {
            ZonePlan layer = layers[i];
            CompoundTag layerCompound = new CompoundTag();
            layer.writeToNBT(layerCompound);
            nbt.put("layer_" + i, layerCompound);
        }
//        return nbt;
    }

    @Override
//    public void readFromNBT(CompoundTag nbt)
    public void load(CompoundTag nbt) {
//        super.readFromNBT(nbt);
        super.load(nbt);
        for (int i = 0; i < layers.length; i++) {
            ZonePlan layer = layers[i];
            layer.readFromNBT(nbt.getCompound("layer_" + i));
        }
    }

    public void sendLayerToServer(int index) {
        IMessage message = createMessage(NET_PLAN_CHANGE, (buffer) ->
        {
            buffer.writeShort(index);
            layers[index].writeToByteBuf(buffer);
        });
        MessageManager.sendToServer(message);
    }

    @Override
//    public void getDebugInfo(List<String> left, List<String> right, Direction side)
    public void getDebugInfo(List<Component> left, List<Component> right, Direction side) {
//        left.add("progress_input = " + progressInput);
        left.add(new TextComponent("progress_input = " + progressInput));
//        left.add("progress_output = " + progressOutput);
        left.add(new TextComponent("progress_output = " + progressOutput));
    }

    @Override
    public void update() {
        ITickable.super.update();
        deltaManager.tick();
//        if (getWorld().isRemote)
        if (getLevel().isClientSide) {
            return;
        }

        {
            // noinspection ConstantConditions
            if (!invInputPaintbrush.getStackInSlot(0).isEmpty() && invInputPaintbrush.getStackInSlot(0).getItem() instanceof ItemPaintbrush_BC8 && !invInputMapLocation.getStackInSlot(0).isEmpty()
//                    && invInputMapLocation.getStackInSlot(0).getItem() instanceof ItemMapLocation && invInputMapLocation.getStackInSlot(0).getTag() != null && invInputMapLocation.getStackInSlot(0)
                    && invInputMapLocation.getStackInSlot(0).getItem() instanceof ItemMapLocation && invInputMapLocation.getStackInSlot(0).hasTag() && invInputMapLocation.getStackInSlot(0)
                    .getTag().contains("chunkMapping") && invInputResult.getStackInSlot(0).isEmpty())
            {
                if (progressInput == 0) {
                    deltaProgressInput.addDelta(0, 200, 1);
                    deltaProgressInput.addDelta(200, 205, -1);
                }

                if (progressInput < 200) {
                    progressInput++;
                    return;
                }

                ZonePlan zonePlan = new ZonePlan();
                zonePlan.readFromNBT(invInputMapLocation.getStackInSlot(0).getTag());
//                layers[BCCoreItems.paintbrushClean.get().getBrushFromStack(invInputPaintbrush.getStackInSlot(0)).colour.getMetadata()] = zonePlan.getWithOffset(-pos.getX(), -pos.getZ());
                layers[BCCoreItems.paintbrushClean.get().getBrushFromStack(invInputPaintbrush.getStackInSlot(0)).colour.getId()] = zonePlan.getWithOffset(-worldPosition.getX(), -worldPosition.getZ());
                invInputMapLocation.setStackInSlot(0, StackUtil.EMPTY);
                invInputResult.setStackInSlot(0, new ItemStack(BCCoreItems.mapLocation.get()));
//                this.markDirty();
                this.setChanged();
                this.sendNetworkUpdate(NET_RENDER_DATA);
                progressInput = 0;
            } else if (progressInput != -1) {
                progressInput = -1;
                deltaProgressInput.setValue(0);
            }
        }
        {
            if (!invOutputPaintbrush.getStackInSlot(0).isEmpty() && invOutputPaintbrush.getStackInSlot(0).getItem() instanceof ItemPaintbrush_BC8 && !invOutputMapLocation.getStackInSlot(0).isEmpty()
                    && invOutputMapLocation.getStackInSlot(0).getItem() instanceof ItemMapLocation && invOutputResult.getStackInSlot(0).isEmpty())
            {
                if (progressOutput == 0) {
                    deltaProgressOutput.addDelta(0, 200, 1);
                    deltaProgressOutput.addDelta(200, 205, -1);
                }

                if (progressOutput < 200) {
                    progressOutput++;
                    return;
                }

//                ItemMapLocation.setZone(invOutputMapLocation.getStackInSlot(0), layers[BCCoreItems.paintbrush.getBrushFromStack(invOutputPaintbrush.getStackInSlot(0)).colour.getMetadata()]
                ItemMapLocation.setZone(invOutputMapLocation.getStackInSlot(0), layers[BCCoreItems.paintbrushClean.get().getBrushFromStack(invOutputPaintbrush.getStackInSlot(0)).colour.getId()]
//                        .getWithOffset(pos.getX(), pos.getZ()));
                        .getWithOffset(worldPosition.getX(), worldPosition.getZ()));
                invOutputResult.setStackInSlot(0, invOutputMapLocation.getStackInSlot(0));
                invOutputMapLocation.setStackInSlot(0, StackUtil.EMPTY);
                progressOutput = 0;
            } else if (progressOutput != -1) {
                progressOutput = -1;
                deltaProgressOutput.setValue(0);
            }
        }
    }

    // MenuProvider

    @Override
    public Component getDisplayName() {
        return this.getBlockState().getBlock().getName();
    }

    @Nullable
    @Override
    public ContainerZonePlanner createMenu(int id, Inventory inventory, Player player) {
        return new ContainerZonePlanner(BCRoboticsMenuTypes.ZONE_PLANNER, id, player, this);
    }
}
