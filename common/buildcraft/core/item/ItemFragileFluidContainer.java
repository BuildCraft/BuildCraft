package buildcraft.core.item;

import buildcraft.api.items.IItemFluidShard;
import buildcraft.lib.fluid.BCFluid;
import buildcraft.lib.fluid.BCFluidAttributes;
import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.StackUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemFragileFluidContainer extends ItemBC_Neptune implements IItemFluidShard {

    // Half of a bucket
    public static final int MAX_FLUID_HELD = 500;

    public ItemFragileFluidContainer(String idBC, Item.Properties properties) {
        super(idBC, properties);
//        setMaxStackSize(1);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt) {
        return new FragileFluidHandler(stack);
    }

    @Override
    protected void addSubItems(ItemGroup tab, NonNullList<ItemStack> items) {
        // Never allow this to be displayed in a creative tab -- we don't want to list every single fluid...
    }

    @Override
//    public String getItemStackDisplayName(ItemStack stack)
    public ITextComponent getName(ItemStack stack) {
        FluidStack fluid = getFluid(stack);

//        String localized;
        ITextComponent localized;

        if (fluid == null) {
//            localized = "ERROR! NULL FLUID!";
            localized = new StringTextComponent("ERROR! NULL FLUID!");
        } else if (fluid.getRawFluid() instanceof BCFluid) {
            BCFluid bcFluid = (BCFluid) fluid.getRawFluid();
            if (((BCFluidAttributes) bcFluid.getAttributes()).isHeatable()) {
                // Add the heatable bit to the end of the name
//                localized = bcFluid.getBareLocalizedName(fluid);
                localized = bcFluid.getAttributes().getDisplayName(fluid);
//                String whole = LocaleUtil.localize(getUnlocalizedName() + ".name", localized);
//                return whole + LocaleUtil.localize("buildcraft.fluid.heat_" + bcFluid.getHeatValue());
                return new TranslationTextComponent(getDescriptionId(stack), localized);
            } else {
//                localized = fluid.getDisplayName().getString();
                localized = fluid.getDisplayName();
            }
        } else {
//            localized = fluid.getDisplayName().getString();
            localized = fluid.getDisplayName();
        }
//        return new StringTextComponent(LocaleUtil.localize(this.getDescriptionId(stack), localized));
        return new TranslationTextComponent(this.getDescriptionId(stack), localized);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
//    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        CompoundNBT fluidTag = stack.getTagElement("fluid");
        if (fluidTag != null) {
            FluidStack fluid = FluidStack.loadFluidStackFromNBT(fluidTag);
            if (fluid != null && fluid.getAmount() > 0) {
//                tooltip.add(LocaleUtil.localizeFluidStaticAmount(fluid.amount, MAX_FLUID_HELD));
                tooltip.add(LocaleUtil.localizeFluidStaticAmountComponent(fluid.getAmount(), MAX_FLUID_HELD));
            }
        }
    }

    @Override
    public void addFluidDrops(NonNullList<ItemStack> toDrop, FluidStack fluid) {
        if (fluid == null) {
            return;
        }
        int amount = fluid.getAmount();
        if (amount >= MAX_FLUID_HELD) {
            FluidStack fluid2 = fluid.copy();
            fluid2.setAmount(MAX_FLUID_HELD);
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
        CompoundNBT nbt = NBTUtilBC.getItemData(container);
        nbt.put("fluid", fluid.writeToNBT(new CompoundNBT()));
    }

    @Nullable
    static FluidStack getFluid(ItemStack container) {
        if (container.isEmpty()) {
            return null;
        }
        CompoundNBT fluidNbt = container.getTagElement("fluid");
        if (fluidNbt == null) {
            return null;
        }
        return FluidStack.loadFluidStackFromNBT(fluidNbt);
    }

    public class FragileFluidHandler implements IFluidHandlerItem, ICapabilityProvider {

        @javax.annotation.Nonnull
        private ItemStack container;

        public FragileFluidHandler(@javax.annotation.Nonnull ItemStack container) {
            this.container = container;
        }

//        @Override
//        public boolean hasCapability(Capability<?> capability, Direction facing) {
//            return getCapability(capability, facing).isPresent();
//        }

        @Override
//        public <T> T getCapability(Capability<T> capability, Direction facing)
        public <T> LazyOptional<T> getCapability(@javax.annotation.Nonnull final Capability<T> capability, final @Nullable Direction side) {
            if (capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY
                    || capability == CapUtil.CAP_FLUIDS) {
                return LazyOptional.of(() -> this).cast();
            }
            return LazyOptional.empty();
        }

        // 1.18.2: divided into 3 methods
//        @Override
//        public IFluidTankProperties[] getTankProperties() {
//            return new IFluidTankProperties[] {
//                    new FluidTankProperties(getFluid(container), MAX_FLUID_HELD, false, true) };
//        }

        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public int getTankCapacity(int tank) {
            return MAX_FLUID_HELD;
        }

        @Nonnull
        @Override
        public FluidStack getFluidInTank(int tank) {
            FluidStack fluid = ItemFragileFluidContainer.getFluid(container);
            return fluid != null ? fluid : StackUtil.EMPTY_FLUID;
        }

        @Override
        public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
            return false;
        }

        @Override
        public int fill(FluidStack resource, FluidAction doFill) {
            return 0;
        }

        @Nonnull
        @Override
        public FluidStack drain(FluidStack resource, FluidAction doDrain) {
            FluidStack fluid = ItemFragileFluidContainer.getFluid(container);
            if (fluid == null || resource == null) {
                return StackUtil.EMPTY_FLUID;
            }
            if (!fluid.isFluidEqual(resource)) {
                return StackUtil.EMPTY_FLUID;
            }
            return drain(resource.getAmount(), doDrain);
        }

        @Nonnull
        @Override
        public FluidStack drain(int maxDrain, FluidAction doDrain) {
            FluidStack fluid = ItemFragileFluidContainer.getFluid(container);
            if (fluid == null || maxDrain <= 0) {
                return StackUtil.EMPTY_FLUID;
            }
            int toDrain = Math.min(maxDrain, fluid.getAmount());
            FluidStack f = new FluidStack(fluid, toDrain);
            if (doDrain.execute()) {
                fluid.setAmount(fluid.getAmount() - toDrain);
                if (fluid.getAmount() <= 0) {
                    fluid = StackUtil.EMPTY_FLUID;
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
