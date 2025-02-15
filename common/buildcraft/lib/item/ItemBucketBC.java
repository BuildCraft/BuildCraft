package buildcraft.lib.item;

import buildcraft.lib.registry.CreativeTabManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class ItemBucketBC extends BucketItem {
    public ItemBucketBC(Supplier<? extends Fluid> supplier, Properties properties) {
//        super(supplier, properties.tab(CreativeTabManager.getTab("vanilla.misc")));
        super(supplier, properties);
        CreativeTabManager.addItem(CreativeTabManager.getTab("vanilla.tools_and_utilities"), this);
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.buildcraft.bucket_filled", getFluid().getFluidType().getDescription().getString());
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new FluidBucketWrapper(stack);
    }

    // Calen 1.20.1
    public ResourceLocation getRegistryName() {
        return ForgeRegistries.ITEMS.getKey(this);
    }
}
