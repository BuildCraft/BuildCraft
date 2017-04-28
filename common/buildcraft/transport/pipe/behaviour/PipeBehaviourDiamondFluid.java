package buildcraft.transport.pipe.behaviour;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeEventFluid;
import buildcraft.api.transport.pipe.PipeEventHandler;

public class PipeBehaviourDiamondFluid extends PipeBehaviourDiamond {
    public PipeBehaviourDiamondFluid(IPipe pipe, NBTTagCompound nbt) {
        super(pipe, nbt);
    }

    public PipeBehaviourDiamondFluid(IPipe pipe) {
        super(pipe);
    }

    @PipeEventHandler
    public void sideCheck(PipeEventFluid.SideCheck sideCheck) {
        FluidStack toCompare = sideCheck.fluid;
        for (EnumFacing face : EnumFacing.VALUES) {
            if (sideCheck.isAllowed(face) && pipe.isConnected(face)) {
                int offset = FILTERS_PER_SIDE * face.ordinal();
                boolean sideAllowed = false;
                boolean foundItem = false;
                for (int i = 0; i < FILTERS_PER_SIDE; i++) {
                    ItemStack compareTo = filters.getStackInSlot(offset + i);
                    if (compareTo.isEmpty()) continue;
                    FluidStack target = FluidUtil.getFluidContained(compareTo);
                    if (target == null || target.amount <= 0) {
                        continue;
                    }
                    foundItem = true;
                    if (target.isFluidEqual(toCompare)) {
                        sideAllowed = true;
                        break;
                    }
                }
                if (foundItem) {
                    if (sideAllowed) {
                        sideCheck.increasePriority(face, 12);
                    } else {
                        sideCheck.disallow(face);
                    }
                }
            }
        }
    }
}
