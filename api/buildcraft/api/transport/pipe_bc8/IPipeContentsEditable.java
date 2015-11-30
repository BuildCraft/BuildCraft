package buildcraft.api.transport.pipe_bc8;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fluids.Fluid;

import buildcraft.api.core.INBTLoadable_BC8;
import buildcraft.api.core.INetworkLoadable_BC8;
import buildcraft.api.transport.pipe_bc8.IPipePropertyProvider.IPipePropertyProviderEditable;

import io.netty.buffer.ByteBuf;

public interface IPipeContentsEditable extends IPipeContents, INetworkLoadable_BC8<IPipeContentsEditable>, INBTLoadable_BC8<IPipeContentsEditable> {
    IPipeContents asReadOnly();

    public interface IPipeContentsEditableBulk extends IPipeContentsEditable, IPipeContentsBulk {
        void setPart(EnumPipePart part);
    }

    public interface IPipeContentsEditablePower extends IPipeContentsEditableBulk, IPipeContentsPower {
        void setPower(int newPower);

        @Override
        IPipeContentsPower asReadOnly();

        @Override
        IPipeContentsEditablePower readFromNBT(NBTBase nbt);

        @Override
        IPipeContentsEditablePower readFromByteBuf(ByteBuf buf);
    }

    public interface IPipeContentsEditableFluid extends IPipeContentsEditableBulk, IPipeContentsFluid {
        int setAmount(int newAmount);

        void setFluid(Fluid fluid);

        void setNBT(NBTTagCompound compound);

        void setFluidStack();

        @Override
        IPipeContentsFluid asReadOnly();

        @Override
        IPipeContentsEditableFluid readFromNBT(NBTBase nbt);

        @Override
        IPipeContentsEditableFluid readFromByteBuf(ByteBuf buf);
    }

    public interface IPipeContentsEditableSeperate extends IPipeContentsEditable, IPipeContentsSeperate {
        void setJourneyPart(EnumItemJourneyPart journeyPart);

        void setDirection(EnumFacing direction);

        void setSpeed(double speed);
    }

    public interface IPipeContentsEditableItem extends IPipeContentsEditableSeperate, IPipeContentsItem {
        void setStack(ItemStack newStack);

        @Override
        IPipePropertyProviderEditable getProperties();

        @Override
        IPipeContentsItem asReadOnly();

        @Override
        IPipeContentsEditableItem readFromNBT(NBTBase nbt);

        @Override
        IPipeContentsEditableItem readFromByteBuf(ByteBuf buf);
    }
}
