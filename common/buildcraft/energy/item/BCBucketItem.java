package buildcraft.energy.item;

import buildcraft.lib.fluid.BCFluidAttributes;
import buildcraft.lib.registry.CreativeTabManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class BCBucketItem extends BucketItem {
    public BCBucketItem(Supplier<? extends Fluid> supplier, Properties properties) {
        super(supplier, properties.tab(CreativeTabManager.getTab("vanilla.misc")));
    }

    @Override
    public Component getName(ItemStack stack) {
        return new TranslatableComponent("item.buildcraft.bucket_filled", ((BCFluidAttributes) getFluid().getAttributes()).getDisplayName().getString());
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new FluidBucketWrapper(stack);
    }
}
