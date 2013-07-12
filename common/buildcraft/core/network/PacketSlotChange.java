package buildcraft.core.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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
	public void writeData(DataOutputStream data) throws IOException {

		super.writeData(data);

		data.writeInt(slot);
		if (stack != null) {
			data.writeInt(stack.itemID);
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
	public void readData(DataInputStream data) throws IOException {

		super.readData(data);

		this.slot = data.readInt();
		int id = data.readInt();

		if (id != 0) {
			stack = new ItemStack(id, data.readInt(), data.readInt());
			
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
