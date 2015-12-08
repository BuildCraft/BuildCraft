package buildcraft.api.transport.pipe_bc8;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import buildcraft.api.core.EnumPipePart;

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
        EnumItemJourneyPart getJourneyPart();

        EnumFacing getDirection();

        /** This should NEVER return numbers less than or equal to 0!
         * 
         * @return The number of minecraft blocks this contents moves per minecraft ticks */
        double getSpeed();
    }

    public interface IPipeContentsItem extends IPipeContentsSeperate {
        ItemStack cloneItemStack();

        IPipePropertyProvider getProperties();
    }
}
