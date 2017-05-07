package buildcraft.factory.tile;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.EnumPipePart;

import buildcraft.lib.fluid.Tank;
import buildcraft.lib.fluid.TankManager;
import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.misc.StackUtil;

public class TileAutoWorkbenchFluids extends TileAutoWorkbenchBase {
    private final Tank tank1 = new Tank("tank1", Fluid.BUCKET_VOLUME * 6, this);
    private final Tank tank2 = new Tank("tank2", Fluid.BUCKET_VOLUME * 6, this);
    private final TankManager<Tank> tankManager = new TankManager<>(tank1, tank2);

    public TileAutoWorkbenchFluids() {
        super(4);
        caps.addCapability(CapUtil.CAP_FLUIDS, tankManager, EnumPipePart.CENTER);
        caps.addCapability(CapUtil.CAP_FLUIDS, tank1, EnumPipePart.DOWN, EnumPipePart.NORTH, EnumPipePart.WEST);
        caps.addCapability(CapUtil.CAP_FLUIDS, tank2, EnumPipePart.UP, EnumPipePart.SOUTH, EnumPipePart.EAST);
    }

    @Override
    protected WorkbenchCrafting createCrafting() {
        return new WorkbenchCraftingFluids(2, 2);
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
            if (stack.isEmpty()) {
                return super.getBoundVersion();
            }
            ItemStack copied = stack.copy();
            if (copied.getCount() != 1) {
                copied.setCount(1);
            }
            IFluidHandlerItem fluidHandlerItem = FluidUtil.getFluidHandler(stack.copy());
            if (fluidHandlerItem != null) {
                FluidStack fluid = fluidHandlerItem.drain(Integer.MAX_VALUE, true);
                if (fluid != null) {
                    if (fluidHandlerItem.getContainer().isEmpty()) {
                        /* We removed an itemstack -- perhaps the container itself was used up in crafting */
                    } else {
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
            return StackUtil.EMPTY;
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
