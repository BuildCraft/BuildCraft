package buildcraft.core.item;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import buildcraft.lib.item.ItemStackHelper;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import buildcraft.api.items.IItemFluidShard;

import buildcraft.lib.fluid.BCFluid;
import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.NBTUtilBC;

public class ItemFragileFluidContainer extends ItemBC_Neptune implements IItemFluidShard {

    // Half of a bucket
    public static final int MAX_FLUID_HELD = 500;

    public ItemFragileFluidContainer(String id) {
        super(id);
        setMaxStackSize(1);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
        return new FragileFluidHandler(stack);
    }

    @Override
    protected void addSubItems(CreativeTabs tab, List<ItemStack> items) {
        // Never allow this to be displayed in a creative tab -- we don't want to list every single fluid...
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        FluidStack fluid = getFluid(stack);

        String localized;

        if (fluid == null) {
            localized = "ERROR! NULL FLUID!";
        } else if (fluid.getFluid() instanceof BCFluid) {
            BCFluid bcFluid = (BCFluid) fluid.getFluid();
            if (bcFluid.isHeatable()) {
                // Add the heatable bit to the end of the name
                localized = bcFluid.getBareLocalizedName(fluid);
                String whole = LocaleUtil.localize(getUnlocalizedName() + ".name", localized);
                return whole + LocaleUtil.localize("buildcraft.fluid.heat_" + bcFluid.getHeatValue());
            } else {
                localized = fluid.getLocalizedName();
            }
        } else {
            localized = fluid.getLocalizedName();
        }
        return LocaleUtil.localize(getUnlocalizedName() + ".name", localized);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, playerIn, tooltip, advanced);
        NBTTagCompound fluidTag = stack.getSubCompound("fluid", false);
        if (fluidTag != null) {
            FluidStack fluid = FluidStack.loadFluidStackFromNBT(fluidTag);
            if (fluid != null && fluid.amount > 0) {
                tooltip.add(LocaleUtil.localizeFluidStaticAmount(fluid.amount, MAX_FLUID_HELD));
            }
        }
    }

    @Override
    public void addFluidDrops(List<ItemStack> toDrop, FluidStack fluid) {
        if (fluid == null) {
            return;
        }
        int amount = fluid.amount;
        if (amount >= MAX_FLUID_HELD) {
            FluidStack fluid2 = fluid.copy();
            fluid2.amount = MAX_FLUID_HELD;
            while (amount >= MAX_FLUID_HELD) {
                ItemStack stack = new ItemStack(this);
                setFluid(stack, fluid2);
                amount -= MAX_FLUID_HELD;
                toDrop.add(stack);
            }
        }
        if (amount > 0) {
            ItemStack stack = new ItemStack(this);
            setFluid(stack, new FluidStack(fluid, amount));
            toDrop.add(stack);
        }
    }

    static void setFluid(ItemStack container, FluidStack fluid) {
        NBTTagCompound nbt = NBTUtilBC.getItemData(container);
        nbt.setTag("fluid", fluid.writeToNBT(new NBTTagCompound()));
    }

    @Nullable
    static FluidStack getFluid(ItemStack container) {
        if (ItemStackHelper.isEmpty(container)) {
            return null;
        }
        NBTTagCompound fluidNbt = container.getSubCompound("fluid", false);
        if (fluidNbt == null) {
            return null;
        }
        return FluidStack.loadFluidStackFromNBT(fluidNbt);
    }

    public class FragileFluidHandler implements IFluidHandler, ICapabilityProvider {

        @Nonnull
        private ItemStack container;

        public FragileFluidHandler(@Nonnull ItemStack container) {
            this.container = container;
        }

        @Override
        public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
            return getCapability(capability, facing) != null;
        }

        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
            if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY
                    || capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
                return (T) this;
            }
            return null;
        }

        @Override
        public IFluidTankProperties[] getTankProperties() {
            return new IFluidTankProperties[] {
                    new FluidTankProperties(getFluid(container), MAX_FLUID_HELD, false, true) };
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            return 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            FluidStack fluid = ItemFragileFluidContainer.getFluid(container);
            if (fluid == null || resource == null) {
                return null;
            }
            if (!fluid.isFluidEqual(resource)) {
                return null;
            }
            return drain(resource.amount, doDrain);
        }

        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            FluidStack fluid = ItemFragileFluidContainer.getFluid(container);
            if (fluid == null || maxDrain <= 0) {
                return null;
            }
            int toDrain = Math.min(maxDrain, fluid.amount);
            FluidStack f = new FluidStack(fluid, toDrain);
            if (doDrain) {
                fluid.amount -= toDrain;
                if (fluid.amount <= 0) {
                    fluid = null;
                    container = null;
                } else {
                    setFluid(container, fluid);
                }
            }
            return f;
        }

    }
}
