// TODO(R.Chen): blocked by Forge fluid capability (ICapabilityProvider/IFluidHandlerItem/FluidStackBC) — Phase 3 fluid migration
package buildcraft.core.item;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Direction;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import buildcraft.lib.compat.FluidStackBC;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import buildcraft.api.items.IItemFluidShard;

import buildcraft.lib.fluid.BCFluid;
import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.StackUtil;

public class ItemFragileFluidContainer extends ItemBC_Neptune implements IItemFluidShard {

    // Half of a bucket
    public static final int MAX_FLUID_HELD = 500;

    public ItemFragileFluidContainer(String id) {
        super(id);
        setMaxStackSize(1);
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public ICapabilityProvider initCapabilities(ItemStack stack, NbtCompound nbt) {
        return new FragileFluidHandler(stack);
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    protected void addSubItems(ItemGroup tab, DefaultedList<ItemStack> items) {
        // Never allow this to be displayed in a creative tab -- we don't want to list every single fluid...
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public String getItemStackDisplayName(ItemStack stack) {
        FluidStackBC fluid = getFluid(stack);

        String localized;

        if (fluid == null) {
            localized = "ERROR! NULL FLUID!";
        } else if (fluid.getFluid() instanceof BCFluid) {
            BCFluid bcFluid = (BCFluid) fluid.getFluid();
            if (bcFluid.isHeatable()) {
                // Add the heatable bit to the end of the name
                localized = bcFluid.getBareLocalizedName(fluid);
                String whole = LocaleUtil.localize(getUnlocalizedName() + ".name", localized);
                return LocaleUtil.localize("buildcraft.fluid.heat_" + bcFluid.getHeatValue(), whole);
            } else {
                localized = fluid.getLocalizedName();
            }
        } else {
            localized = fluid.getLocalizedName();
        }
        return LocaleUtil.localize(getUnlocalizedName() + ".name", localized);
    }

    @Environment(EnvType.CLIENT)
    // @Override -- removed: method does not exist in Fabric 1.20.1
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, TooltipContext flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        NbtCompound fluidTag = stack.getSubCompound("fluid");
        if (fluidTag != null) {
            FluidStackBC fluid = FluidStackBC.loadFluidStackFromNBT(fluidTag);
            if (fluid != null && fluid.amount > 0) {
                tooltip.add(LocaleUtil.localizeFluidStaticAmount(fluid.amount, MAX_FLUID_HELD));
            }
        }
    }

    @Override
    public void addFluidDrops(DefaultedList<ItemStack> toDrop, FluidStackBC fluid) {
        if (fluid == null) {
            return;
        }
        int amount = fluid.amount;
        if (amount >= MAX_FLUID_HELD) {
            FluidStackBC fluid2 = fluid.copy();
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
            setFluid(stack, new FluidStackBC(fluid, amount));
            toDrop.add(stack);
        }
    }

    static void setFluid(ItemStack container, FluidStackBC fluid) {
        NbtCompound nbt = NBTUtilBC.getItemData(container);
        nbt.put("fluid", fluid.writeToNBT(new NbtCompound()));
    }

    @Nullable
    static FluidStackBC getFluid(ItemStack container) {
        if (container.isEmpty()) {
            return null;
        }
        NbtCompound fluidNbt = container.getSubCompound("fluid");
        if (fluidNbt == null) {
            return null;
        }
        return FluidStackBC.loadFluidStackFromNBT(fluidNbt);
    }

    public class FragileFluidHandler implements IFluidHandlerItem, ICapabilityProvider {

        @Nonnull
        private ItemStack container;

        public FragileFluidHandler(@Nonnull ItemStack container) {
            this.container = container;
        }

        // @Override -- removed: method does not exist in Fabric 1.20.1
        public boolean hasCapability(Capability<?> capability, Direction facing) {
            return getCapability(capability, facing) != null;
        }

        @Override
        public <T> T getCapability(Capability<T> capability, Direction facing) {
            if (capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY
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
        public int fill(FluidStackBC resource, boolean doFill) {
            return 0;
        }

        @Override
        public FluidStackBC drain(FluidStackBC resource, boolean doDrain) {
            FluidStackBC fluid = ItemFragileFluidContainer.getFluid(container);
            if (fluid == null || resource == null) {
                return null;
            }
            if (!fluid.isFluidEqual(resource)) {
                return null;
            }
            return drain(resource.amount, doDrain);
        }

        @Override
        public FluidStackBC drain(int maxDrain, boolean doDrain) {
            FluidStackBC fluid = ItemFragileFluidContainer.getFluid(container);
            if (fluid == null || maxDrain <= 0) {
                return null;
            }
            int toDrain = Math.min(maxDrain, fluid.amount);
            FluidStackBC f = new FluidStackBC(fluid, toDrain);
            if (doDrain) {
                fluid.amount -= toDrain;
                if (fluid.amount <= 0) {
                    fluid = null;
                    container = StackUtil.EMPTY;
                } else {
                    setFluid(container, fluid);
                }
            }
            return f;
        }

        @Override
        public ItemStack getContainer() {
            return container;
        }
    }
}
