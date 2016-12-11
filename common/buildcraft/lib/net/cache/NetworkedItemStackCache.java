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
        if (StackUtil.isInvalid(obj.baseStack)) {
            return ItemStackKey.INVALID_STACK;
        }
        ItemStack stack = obj.baseStack.copy();
        stack.stackSize = 1;
        if (stack.hasTagCompound()) {
            stack.setTagCompound(StackUtil.stripNonFunctionNbt(stack));
        }
        return new ItemStackKey(stack);
    }

    @Override
    protected void writeObject(ItemStackKey obj, PacketBufferBC buffer) {
        if (StackUtil.isInvalid(obj.baseStack)) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            buffer.writeItemStackToBuffer(obj.baseStack);
        }
    }

    @Override
    protected ItemStackKey readObject(PacketBufferBC buffer) throws IOException {
        if (buffer.readBoolean()) {
            return new ItemStackKey(buffer.readItemStackFromBuffer());
        } else {
            return ItemStackKey.INVALID_STACK;
        }
    }

    @Override
    protected String getCacheName() {
        return "ItemStack";
    }
}
