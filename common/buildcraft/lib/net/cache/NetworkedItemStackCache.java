package buildcraft.lib.net.cache;

import java.io.IOException;

import net.minecraft.item.ItemStack;

import buildcraft.lib.misc.ItemStackKey;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.net.PacketBufferBC;

// We use ItemStackKey here because ItemStack doesn't implement hashCode and equals
public class NetworkedItemStackCache extends NetworkedObjectCache<ItemStackKey> {

    public NetworkedItemStackCache() {
        super(new ItemStackKey(StackUtil.INVALID_STACK));
    }

    @Override
    protected ItemStackKey getCanonical(ItemStackKey obj) {
        ItemStack stack = obj.baseStack.copy();
        stack.stackSize = 1;
        if (stack.hasTagCompound()) {
            stack.setTagCompound(StackUtil.stripNonFunctionNbt(stack));
        }
        return new ItemStackKey(stack);
    }

    @Override
    protected void writeObject(ItemStackKey obj, PacketBufferBC buffer) {
        buffer.writeItemStackToBuffer(obj.baseStack);
    }

    @Override
    protected ItemStackKey readObject(PacketBufferBC buffer) throws IOException {
        return new ItemStackKey(buffer.readItemStackFromBuffer());
    }
}
