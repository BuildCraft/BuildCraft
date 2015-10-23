/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import buildcraft.core.lib.utils.NetworkUtils;

import io.netty.buffer.ByteBuf;

public class PacketSlotChange extends PacketCoordinates {
    public interface ITile {
        void updateCraftingMatrix(int slot, ItemStack stack);
    }

    public int slot;
    public ItemStack stack;

    public PacketSlotChange() {}

    public PacketSlotChange(TileEntity tile, int slot, ItemStack stack) {
        super(tile);
        this.slot = slot;
        this.stack = stack;
    }

    @Override
    public void writeData(ByteBuf data, World world, EntityPlayer player) {
        super.writeData(data, world, player);

        data.writeShort(slot);
        NetworkUtils.writeStack(data, stack);
    }

    @Override
    public void readData(ByteBuf data) {
        super.readData(data);

        this.slot = data.readUnsignedShort();
        stack = NetworkUtils.readStack(data);
    }

    @Override
    public void applyData(World world, EntityPlayer player) {
        if (!world.isBlockLoaded(pos)) {
            return;
        }
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof ITile) {
            ((ITile) tile).updateCraftingMatrix(slot, stack);
        }
    }
}
