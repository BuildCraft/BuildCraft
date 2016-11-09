package buildcraft.robotics.tile;

import java.io.IOException;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.tiles.IDebuggable;

import buildcraft.core.BCCoreItems;
import buildcraft.core.item.ItemMapLocation;
import buildcraft.core.item.ItemPaintbrush_BC8;
import buildcraft.lib.delta.DeltaInt;
import buildcraft.lib.delta.DeltaManager;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.tile.TileBCInventory_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import buildcraft.robotics.zone.ZonePlan;
import buildcraft.robotics.zone.ZonePlannerMapChunkKey;

public class TileZonePlanner extends TileBCInventory_Neptune implements ITickable, IDebuggable {
    public static final int NET_PLAN_CHANGE = 10;

    public final ItemHandlerSimple invPaintbrushes;
    public final ItemHandlerSimple invInputPaintbrush;
    public final ItemHandlerSimple invInputMapLocation;
    public final ItemHandlerSimple invInputResult;
    public final ItemHandlerSimple invOutputPaintbrush;
    public final ItemHandlerSimple invOutputMapLocation;
    public final ItemHandlerSimple invOutputResult;
    private int progressInput = 0;
    public final DeltaInt deltaProgressInput = deltaManager.addDelta("progressInput", DeltaManager.EnumNetworkVisibility.GUI_ONLY);
    private int progressOutput = 0;
    public final DeltaInt deltaProgressOutput = deltaManager.addDelta("progressOutput", DeltaManager.EnumNetworkVisibility.GUI_ONLY);
    public ZonePlan[] layers = new ZonePlan[16];

    public TileZonePlanner() {
        invPaintbrushes = addInventory("paintbrushes", 16, ItemHandlerManager.EnumAccess.NONE);
        invInputPaintbrush = addInventory("inputPaintbrush", 1, ItemHandlerManager.EnumAccess.NONE);
        invInputMapLocation = addInventory("inputMapLocation", 1, ItemHandlerManager.EnumAccess.NONE);
        invInputResult = addInventory("inputResult", 1, ItemHandlerManager.EnumAccess.NONE);
        invOutputPaintbrush = addInventory("outputPaintbrush", 1, ItemHandlerManager.EnumAccess.NONE);
        invOutputMapLocation = addInventory("outputMapLocation", 1, ItemHandlerManager.EnumAccess.NONE);
        invOutputResult = addInventory("outputResult", 1, ItemHandlerManager.EnumAccess.NONE);
        for (int i = 0; i < layers.length; i++) {
            layers[i] = new ZonePlan();
        }
    }

    public int getLevel() {
        BlockPos blockPos = Minecraft.getMinecraft().thePlayer.getPosition();
        while (!Minecraft.getMinecraft().theWorld.getBlockState(blockPos).getBlock().isBlockSolid(Minecraft.getMinecraft().theWorld, blockPos, EnumFacing.DOWN) && blockPos.getY() < 255) {
            blockPos = new BlockPos(blockPos.getX(), blockPos.getY() + 1, blockPos.getZ());
        }
        return (int) Math.floor((double) blockPos.getY() / ZonePlannerMapChunkKey.LEVEL_HEIGHT);
    }

    @Override
    public void writePayload(int id, PacketBuffer buffer, Side side) {
        super.writePayload(id, buffer, side);
        if (side == Side.SERVER) {
            if (id == NET_RENDER_DATA) {
                for (ZonePlan layer : layers) {
                    layer.writeToByteBuf(buffer);
                }
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBuffer buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == Side.CLIENT) {
            if (id == NET_RENDER_DATA) {
                for (int i = 0; i < layers.length; i++) {
                    ZonePlan layer = layers[i];
                    layers[i] = layer.readFromByteBuf(buffer);
                }
            }
        } else if (side == Side.SERVER) {
            if (id == NET_PLAN_CHANGE) {
                int index = buffer.readUnsignedShort();
                layers[index].readFromByteBuf(buffer);
                markDirty();
                sendNetworkUpdate(NET_RENDER_DATA);
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        for (int i = 0; i < layers.length; i++) {
            ZonePlan layer = layers[i];
            NBTTagCompound layerCompound = new NBTTagCompound();
            layer.writeToNBT(layerCompound);
            nbt.setTag("layer_" + i, layerCompound);
        }
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        for (int i = 0; i < layers.length; i++) {
            ZonePlan layer = layers[i];
            layer.readFromNBT(nbt.getCompoundTag("layer_" + i));
        }
    }

    public void sendLayerToServer(int index) {
        IMessage message = createMessage(NET_PLAN_CHANGE, (buffer) -> {
            buffer.writeShort(index);
            layers[index].writeToByteBuf(buffer);
        });
        MessageUtil.getWrapper().sendToServer(message);
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("");
        left.add("progress_input = " + progressInput);
        left.add("progress_output = " + progressOutput);
    }

    @Override
    public void update() {
        deltaManager.tick();
        if (getWorld().isRemote) {
            return;
        }

        {
            ItemStack paintbrushStack = invInputPaintbrush.getStackInSlot(0);
            ItemStack mapLocationStack = invInputMapLocation.getStackInSlot(0);
            if (paintbrushStack != null && paintbrushStack.getItem() instanceof ItemPaintbrush_BC8 && mapLocationStack != null && mapLocationStack.getItem() instanceof ItemMapLocation && mapLocationStack.getTagCompound() != null && mapLocationStack
                    .getTagCompound().hasKey("chunkMapping") && invInputResult.getStackInSlot(0) == null) {
                if (progressInput == 0) {
                    deltaProgressInput.addDelta(0, 200, 100);
                    deltaProgressInput.addDelta(200, 205, -100);
                }

                if (progressInput < 200) {
                    progressInput++;
                    return;
                }

                layers[BCCoreItems.paintbrush.getBrushFromStack(paintbrushStack).colour.getMetadata()].readFromNBT(mapLocationStack.getTagCompound());
                invInputMapLocation.setStackInSlot(0, null);
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
            ItemStack paintbrushStack = invOutputPaintbrush.getStackInSlot(0);
            ItemStack mapLocationStack = invOutputMapLocation.getStackInSlot(0);
            if (paintbrushStack != null && paintbrushStack.getItem() instanceof ItemPaintbrush_BC8 && mapLocationStack != null && mapLocationStack.getItem() instanceof ItemMapLocation && invOutputResult.getStackInSlot(0) == null) {
                if (progressOutput == 0) {
                    deltaProgressOutput.addDelta(0, 200, 100);
                    deltaProgressOutput.addDelta(200, 205, -100);
                }

                if (progressOutput < 200) {
                    progressOutput++;
                    return;
                }

                ItemMapLocation.setZone(mapLocationStack, layers[BCCoreItems.paintbrush.getBrushFromStack(paintbrushStack).colour.getMetadata()]);
                invOutputMapLocation.setStackInSlot(0, null);
                invOutputResult.setStackInSlot(0, mapLocationStack);
                progressOutput = 0;
            } else if (progressOutput != -1) {
                progressOutput = -1;
                deltaProgressOutput.setValue(0);
            }
        }
    }
}
