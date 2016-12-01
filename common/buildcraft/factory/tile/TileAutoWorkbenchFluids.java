package buildcraft.factory.tile;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.AxisDirection;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.fluids.Tank;
import buildcraft.lib.fluids.TankManager;

public class TileAutoWorkbenchFluids extends TileAutoWorkbenchBase {
    private final Tank tank1 = new Tank("tank1", Fluid.BUCKET_VOLUME * 6, this);
    private final Tank tank2 = new Tank("tank2", Fluid.BUCKET_VOLUME * 6, this);
    private final TankManager<Tank> tankManager = new TankManager<>(tank1, tank2);

    public TileAutoWorkbenchFluids() {
        super(4);
    }

    @Override
    protected WorkbenchCrafting createCrafting() {
        return new WorkbenchCraftingFluids(2, 2);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            if (facing == null) {
                return (T) tankManager;
            } else if (facing.getAxisDirection() == AxisDirection.POSITIVE) {
                return (T) tank1;
            } else {
                return (T) tank2;
            }
        }
        return super.getCapability(capability, facing);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        super.getDebugInfo(left, right, side);
        left.add("Tanks:");
        left.add("  " + tank1.getContentsString());
        left.add("  " + tank2.getContentsString());

    }

    // #############################
    //
    // Sub-classes for crafting
    //
    // #############################

    public class WorkbenchCraftingFluids extends WorkbenchCrafting {
        public WorkbenchCraftingFluids(int width, int height) {
            super(width, height);
            for (int i = 0; i < this.craftingSlots.length; i++) {
                this.craftingSlots[i] = new CraftSlotFluid(i);
            }
        }
    }

    public class CraftSlotFluid extends CraftSlotItem {
        public CraftSlotFluid(int slot) {
            super(slot);
        }

        @Override
        public CraftingSlot getBoundVersion() {
            ItemStack stack = get();
            if (stack != null && stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null)) {
                ItemStack copied = stack.copy();
                IFluidHandler handler = copied.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
                FluidStack fluid = handler.drain(8000, true);
                if (fluid != null) {
                    if (copied.getCount() == stack.getCount()) {
                        // This was NOT used up, so we can just ignore the stack altogether and return a fluid version
                        return new CraftSlotFluidBound(this, fluid);
                    }
                }
            }
            return super.getBoundVersion();
        }
    }

    public class CraftSlotFluidBound extends CraftingSlot {
        protected final CraftSlotFluid nonBound;
        protected final FluidStack fluidUsed;

        public CraftSlotFluidBound(CraftSlotFluid from, FluidStack fluidUsed) {
            super(-1);
            this.fluidUsed = fluidUsed;
            this.nonBound = from;
        }

        @Override
        public ItemStack get() {
            FluidStack drained = tankManager.drain(fluidUsed, false);
            if (drained != null && drained.amount == fluidUsed.amount) {
                return nonBound.get().copy();
            }
            return null;
        }

        @Override
        public void use(int count) {
            if (count == 1) {
                tankManager.drain(fluidUsed, true);
            }
        }

        @Override
        public CraftingSlot getBoundVersion() {
            return nonBound.getBoundVersion();
        }

        @Override
        public CraftingSlot getUnboundVersion() {
            return nonBound;
        }
    }
}
