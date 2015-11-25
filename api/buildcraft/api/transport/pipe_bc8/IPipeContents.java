package buildcraft.api.transport.pipe_bc8;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

public interface IPipeContents {
    public interface IPipeContentsBulk extends IPipeContents {
        /** Get the part of the pipe the contents is held within */
        EnumPipePart getPart();
    }

    public interface IPipeContentsPower extends IPipeContentsBulk {
        int powerHeld();
    }

    public interface IPipeContentsFluid extends IPipeContentsBulk {
        int getAmount();

        Fluid getFluid();

        NBTTagCompound getNBT();

        FluidStack cloneFluidStack();
    }

    public interface IPipeContentsSeperate extends IPipeContents {
        EnumPipeDirection getDirection();

        EnumFacing getPart();
    }

    public interface IPipeContentsItem extends IPipeContentsSeperate {
        ItemStack getStack();

        IPipePropertyProvider getProperties();
    }
}
