package ct.buildcraft.core.item;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import ct.buildcraft.api.items.IItemFluidShard;
import ct.buildcraft.core.BCCore;
import ct.buildcraft.lib.fluid.BCFluid;
import ct.buildcraft.lib.misc.LocaleUtil;
import ct.buildcraft.lib.misc.StackUtil;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.locale.Language;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class ItemFragileFluidContainer extends Item implements IItemFluidShard {

    // Half of a bucket
    public static final int MAX_FLUID_HELD = 500;

    public ItemFragileFluidContainer() {
    	super(new Item.Properties().stacksTo(1).tab(BCCore.BUILDCRAFT_TAB));
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
        return new FragileFluidHandler(stack);
    }

    @Override
	public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> items) {
        // Never allow this to be displayed in a creative tab -- we don't want to list every single fluid...
    }

	@Override
	public String getDescriptionId() {
		return super.getDescriptionId();
	}

	@Override
	public Component getName(ItemStack stack) {
        FluidStack fluid = getFluid(stack);

        String localized;
        Language lang = Language.getInstance();
        if (fluid == null) {
            localized = "ERROR! NULL FLUID!";
        } else if (fluid.getFluid() instanceof BCFluid) {
            BCFluid bcFluid = (BCFluid) fluid.getFluid();
            if (bcFluid.isHeatable()) {
            	
                // Add the heatable bit to the end of the name
                localized = lang.getOrDefault(bcFluid.getFluidType().getDescriptionId());
                Component whole = Component.translatable(getDescriptionId(), localized);
                return Component.empty().append(whole).append(Component.translatable("buildcraft.fluid.heat_" + bcFluid.getHeatValue()));
            } else {
                localized = lang.getOrDefault(fluid.getTranslationKey());
            }
        } else {
            localized = lang.getOrDefault(fluid.getTranslationKey());
        }
        return Component.translatable(getDescriptionId(), localized);
	}

	@OnlyIn(Dist.CLIENT)
    @Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        CompoundTag t = stack.getTag();
        CompoundTag fluidTag = t == null ? null : t.getCompound("fluid");
        if (fluidTag != null) {
            FluidStack fluid = FluidStack.loadFluidStackFromNBT(fluidTag);
            if (fluid != null && fluid.getAmount() > 0) {
                tooltip.add((LocaleUtil.localizeFluidStaticAmount(fluid.getAmount(), MAX_FLUID_HELD)));
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

    public static void setFluid(ItemStack container, FluidStack fluid) {
        CompoundTag nbt =container.getOrCreateTag();
        nbt.put("fluid", fluid.writeToNBT(new CompoundTag()));
    }

    @Nullable
    public static FluidStack getFluid(ItemStack container) {
        if (container.isEmpty()) {
            return FluidStack.EMPTY;
        }
        CompoundTag t = container.getTag();
        CompoundTag fluidNbt = t == null? null : t.getCompound("fluid");
        if (fluidNbt == null) {
            return FluidStack.EMPTY;
        }
        return FluidStack.loadFluidStackFromNBT(fluidNbt);
    }

    public class FragileFluidHandler implements IFluidHandlerItem, ICapabilityProvider {

        @Nonnull
        private ItemStack container;

        public FragileFluidHandler(@Nonnull ItemStack container) {
            this.container = container;
        }


        @Override
        public <T> @NotNull LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
            if (capability == ForgeCapabilities.FLUID_HANDLER_ITEM
                || capability == ForgeCapabilities.FLUID_HANDLER) {
                return  LazyOptional.of(() ->this).cast();
            }
            return LazyOptional.empty();
        }


        @Override
        public int fill(FluidStack resource, FluidAction doFill) {
            return 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction doDrain) {
            FluidStack fluid = ItemFragileFluidContainer.getFluid(container);
            if (fluid == null || resource == null) {
                return FluidStack.EMPTY;
            }
            if (!fluid.isFluidEqual(resource)) {
                return FluidStack.EMPTY;
            }
            return drain(resource.getAmount(), doDrain);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction doDrain) {
            FluidStack fluid = ItemFragileFluidContainer.getFluid(container);
            if (fluid.isEmpty() || maxDrain <= 0) {
                return FluidStack.EMPTY;
            }
            int toDrain = Math.min(maxDrain, fluid.getAmount());
            FluidStack f = new FluidStack(fluid, toDrain);
            if (doDrain.execute()) {
                fluid.setAmount(fluid.getAmount() - toDrain);
                if (fluid.getAmount() <= 0) {
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


		@Override
		public int getTanks() {
			return 1;
		}


		@Override
		public @NotNull FluidStack getFluidInTank(int tank) {
			return ItemFragileFluidContainer.getFluid(container);
		}


		@Override
		public int getTankCapacity(int tank) {
			return MAX_FLUID_HELD;
		}


		@Override
		public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
			return false;
		}
    }
}
