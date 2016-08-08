package buildcraft.robotics.tile;

import buildcraft.lib.BCMessageHandler;
import buildcraft.lib.tile.TileBCInventory_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import buildcraft.robotics.MessageZonePlannerLayer;
import buildcraft.robotics.ZonePlan;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;

import java.io.IOException;

public class TileZonePlanner extends TileBCInventory_Neptune {
    public final ItemHandlerSimple invPaintbrushes;
    public ZonePlan[] layers = new ZonePlan[16];

    public TileZonePlanner() {
        invPaintbrushes = addInventory("paintbrushes", 16, ItemHandlerManager.EnumAccess.NONE);
        for(int i = 0; i < layers.length; i++) {
            layers[i] = new ZonePlan();
        }
    }

    @Override
    public void writePayload(int id, PacketBuffer buffer, Side side) {
        super.writePayload(id, buffer, side);
        if(id == NET_RENDER_DATA) {
            for(ZonePlan layer : layers) {
                layer.writeToByteBuf(buffer);
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBuffer buffer, Side side) throws IOException {
        super.readPayload(id, buffer, side);
        if(id == NET_RENDER_DATA) {
            for(int i = 0; i < layers.length; i++) {
                ZonePlan layer = layers[i];
                layers[i] = layer.readFromByteBuf(buffer);
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        for(int i = 0; i < layers.length; i++) {
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
        for(int i = 0; i < layers.length; i++) {
            ZonePlan layer = layers[i];
            layer.readFromNBT(nbt.getCompoundTag("layer_" + i));
        }
    }

    public void sendLayerToServer(int index) {
        BCMessageHandler.netWrapper.sendToServer(new MessageZonePlannerLayer(pos, index, layers[index]));
    }
}
