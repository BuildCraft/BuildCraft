package buildcraft.api.transport.pipe_bc8;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.fluids.Fluid;

import buildcraft.api.core.INBTStoreable;
import buildcraft.api.core.ISerializable;
import buildcraft.api.transport.pipe_bc8.IPipePropertyProvider.IPipePropertyProviderEditable;

public interface IPipeContentsEditable extends IPipeContents, ISerializable, INBTStoreable {
    IPipeContents asReadOnly();

    public interface IPipeContentsEditableBulk extends IPipeContentsEditable, IPipeContentsBulk {}

    public interface IPipeContentsEditablePower extends IPipeContentsEditableBulk, IPipeContentsPower {
        void setPower(int newPower);

        @Override
        IPipeContentsPower asReadOnly();
    }

    public interface IPipeContentsEditableFluid extends IPipeContentsEditableBulk, IPipeContentsFluid {
        int setAmount(int newAmount);

        void setFluid(Fluid fluid);

        void setNBT(NBTTagCompound compound);

        void setFluidStack();

        @Override
        IPipeContentsFluid asReadOnly();
    }

    public interface IPipeContentsEditableItem extends IPipeContentsEditable, IPipeContentsItem {
        void setStack(ItemStack newStack);

        @Override
        IPipePropertyProviderEditable getProperties();

        @Override
        IPipeContentsItem asReadOnly();
    }
}
