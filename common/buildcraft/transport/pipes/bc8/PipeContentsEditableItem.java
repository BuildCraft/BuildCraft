package buildcraft.transport.pipes.bc8;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.pipe_bc8.EnumItemJourneyPart;
import buildcraft.api.transport.pipe_bc8.IPipeContentsEditable.IPipeContentsEditableItem;
import buildcraft.api.transport.pipe_bc8.IPipePropertyProvider.IPipePropertyProviderEditable;
import buildcraft.core.lib.utils.NBTUtils;

import io.netty.buffer.ByteBuf;

public class PipeContentsEditableItem implements IPipeContentsEditableItem {
    private final IPipePropertyProviderEditable propertyProvider;
    private ItemStack stack;
    private EnumItemJourneyPart journeyPart;
    private EnumFacing direction;
    private double speed;

    public PipeContentsEditableItem(ItemStack stack, EnumItemJourneyPart journeyPart, EnumFacing direction) {
        this(new PipePropertyProviderEditable(), stack, journeyPart, direction, 0.05);
    }

    public PipeContentsEditableItem(IPipePropertyProviderEditable propertyProvider, ItemStack stack, EnumItemJourneyPart journeyPart,
            EnumFacing direction, double speed) {
        this.propertyProvider = propertyProvider;
        this.stack = stack;
        this.journeyPart = journeyPart;
        this.direction = direction;
        this.speed = speed;
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
    public EnumItemJourneyPart getJourneyPart() {
        return journeyPart;
    }

    @Override
    public void setJourneyPart(EnumItemJourneyPart direction) {
        if (direction == null) throw new NullPointerException("direction");
        this.journeyPart = direction;
    }

    @Override
    public EnumFacing getDirection() {
        return direction;
    }

    @Override
    public void setDirection(EnumFacing part) {
        this.direction = part;
    }

    @Override
    public double getSpeed() {
        return speed;
    }

    @Override
    public void setSpeed(double speed) {
        this.speed = speed;
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
        NBTTagCompound tag = (NBTTagCompound) nbt;
        ItemStack stack = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("stack"));
        EnumItemJourneyPart journeyPart = NBTUtils.readEnum(tag.getTag("journeyPart"), EnumItemJourneyPart.class);
        EnumFacing direction = NBTUtils.readEnum(tag.getTag("direction"), EnumFacing.class);
        double speed = tag.getDouble("speed");
        IPipePropertyProviderEditable provider = propertyProvider.readFromNBT(tag.getTag("properties"));
        return new PipeContentsEditableItem(provider, stack, journeyPart, direction, speed);
    }

    @Override
    public NBTBase writeToNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        if (stack != null) nbt.setTag("stack", stack.writeToNBT(new NBTTagCompound()));
        nbt.setTag("journeyPart", NBTUtils.writeEnum(journeyPart));
        nbt.setTag("direction", NBTUtils.writeEnum(direction));
        nbt.setDouble("speed", speed);
        nbt.setTag("properties", propertyProvider.writeToNBT());
        return nbt;
    }
}
