/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import buildcraft.core.lib.network.base.Packet;
import buildcraft.core.lib.utils.NetworkUtils;
import buildcraft.transport.TravelingItem;

import io.netty.buffer.ByteBuf;

public class PacketPipeTransportItemStack extends Packet {

    private ItemStack stack;
    private int entityId;

    public PacketPipeTransportItemStack() {}

    public PacketPipeTransportItemStack(World world, int entityId, ItemStack stack) {
        super(world);
        this.entityId = entityId;
        this.stack = stack;
    }

    @Override
    public void writeData(ByteBuf data) {
        super.writeData(data);
        data.writeInt(entityId);
        NetworkUtils.writeStack(data, stack);
    }

    @Override
    public void readData(ByteBuf data) {
        super.readData(data);
        this.entityId = data.readInt();
        stack = NetworkUtils.readStack(data);
    }

    public int getEntityId() {
        return entityId;
    }

    public ItemStack getItemStack() {
        return stack;
    }

    @Override
    public void applyData(World world, EntityPlayer player) {
        TravelingItem item = TravelingItem.clientCache.get(entityId);
        if (item != null) {
            item.setItemStack(stack);
        }
    }
}
