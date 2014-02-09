package buildcraft.core.network;

import io.netty.buffer.ByteBuf;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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
		if (stack != null) {
			data.writeInt(Item.itemRegistry.getIDForObject(stack.getItem()));
			data.writeInt(stack.stackSize);
			data.writeInt(stack.getItemDamage());
			
			if(stack.hasTagCompound()) {
				byte[] compressed = CompressedStreamTools.compress(stack.getTagCompound());
				data.writeShort(compressed.length);
				data.write(compressed);
			} else {
				data.writeShort(0);
			}

		} else {
			data.writeInt(0);
		}
	}

	@Override
	public void readData(ByteBuf data) {

		super.readData(data);

		this.slot = data.readInt();
		int id = data.readInt();

		if (id != 0) {
			Item item = Item.getItemById(id);
			stack = new ItemStack(item, data.readInt(), data.readInt());
			
			// Yes, this stuff may indeed have NBT and don't you forget it.
			short length = data.readShort();
			
			if(length > 0) {
				byte[] compressed = new byte[length];
				data.readFully(compressed);
				stack.setTagCompound(CompressedStreamTools.decompress(compressed));
			}


		} else {
			stack = null;
		}
	}

}
