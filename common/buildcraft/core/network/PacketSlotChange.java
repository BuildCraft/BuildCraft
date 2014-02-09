package buildcraft.core.network;

import io.netty.buffer.ByteBuf;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import buildcraft.core.utils.Utils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;

public class PacketSlotChange extends PacketCoordinates {

	public int slot;
	public ItemStack stack;

	public PacketSlotChange() {
	}

	public PacketSlotChange(int id, int x, int y, int z, int slot, ItemStack stack) {
		super(id, x, y, z);
		this.slot = slot;
		this.stack = stack;
	}

	@Override
	public void writeData(ByteBuf data) {
		super.writeData(data);

		data.writeInt(slot);
		Utils.writeStack(data, stack);		
	}

	@Override
	public void readData(ByteBuf data) {
		super.readData(data);

		this.slot = data.readInt();
		stack = Utils.readStack(data);
	}
}
