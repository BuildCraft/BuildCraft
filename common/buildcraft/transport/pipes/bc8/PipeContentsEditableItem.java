package buildcraft.transport.pipes.bc8;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.pipe_bc8.EnumPipeDirection;
import buildcraft.api.transport.pipe_bc8.IPipeContentsEditable.IPipeContentsEditableItem;
import buildcraft.api.transport.pipe_bc8.IPipePropertyProvider.IPipePropertyProviderEditable;

import io.netty.buffer.ByteBuf;

public class PipeContentsEditableItem implements IPipeContentsEditableItem {
    private ItemStack stack;
    private EnumPipeDirection direction;
    private EnumFacing part;
    private final IPipePropertyProviderEditable propertyProvider;

    public PipeContentsEditableItem(ItemStack stack, EnumPipeDirection direction, EnumFacing part) {
        this.stack = stack;
        this.direction = direction;
        this.part = part;
        propertyProvider = new PipePropertyProviderEditable();
    }

    @Override
    public ItemStack cloneItemStack() {
        return stack.copy();
    }

    @Override
    public void setStack(ItemStack newStack) {
        this.stack = newStack;
    }

    @Override
    public EnumPipeDirection getDirection() {
        return direction;
    }

    @Override
    public void setPipeDirection(EnumPipeDirection direction) {
        if (direction == null) throw new NullPointerException("direction");
        this.direction = direction;
    }

    @Override
    public EnumFacing getPart() {
        return part;
    }

    @Override
    public void setPart(EnumFacing part) {
        this.part = part;
    }

    @Override
    public IPipePropertyProviderEditable getProperties() {
        return propertyProvider;
    }

    @Override
    public IPipeContentsItem asReadOnly() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IPipeContentsEditableItem readFromByteBuf(ByteBuf buf) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void writeToByteBuf(ByteBuf buf) {
        // TODO Auto-generated method stub

    }

    @Override
    public IPipeContentsEditableItem readFromNBT(NBTBase nbt) {
        return null;
    }

    @Override
    public NBTBase writeToNBT() {
        return null;
    }
}
