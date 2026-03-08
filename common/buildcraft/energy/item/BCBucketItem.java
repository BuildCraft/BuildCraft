package buildcraft.energy.item;

import buildcraft.lib.fluid.BCFluidAttributes;
import buildcraft.lib.registry.CreativeTabManager;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class BCBucketItem extends BucketItem {
    public BCBucketItem(Supplier<? extends Fluid> supplier, Properties properties) {
        super(supplier, properties.tab(CreativeTabManager.getTab("vanilla.misc")));
    }

    @Override
    public ITextComponent getName(ItemStack stack) {
        return new TranslationTextComponent("item.buildcraft.bucket_filled", ((BCFluidAttributes) getFluid().getAttributes()).getDisplayName().getString());
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        return new FluidBucketWrapper(stack);
    }
}
