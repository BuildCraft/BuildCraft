package buildcraft.lib.fluid;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.UniversalBucket;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FluidHelper {

    /**
     * @param fluidStack contents used to fill the bucket.
     *                   FluidStack is used instead of Fluid to preserve fluid NBT, the amount is ignored.
     * @return a filled vanilla bucket or filled universal bucket.
     *         Returns empty itemStack if none of the enabled buckets can hold the fluid.
     */
    @Nullable
    public static ItemStack getFilledBucket(@Nonnull FluidStack fluidStack)
    {
        Fluid fluid = fluidStack.getFluid();

        if (fluidStack.tag == null || fluidStack.tag.hasNoTags())
        {
            if (fluid == FluidRegistry.WATER)
            {
                return new ItemStack(Items.WATER_BUCKET);
            }
            else if (fluid == FluidRegistry.LAVA)
            {
                return new ItemStack(Items.LAVA_BUCKET);
            }
            else if (fluid.getName().equals("milk"))
            {
                return new ItemStack(Items.MILK_BUCKET);
            }
        }

        if (FluidRegistry.isUniversalBucketEnabled() && FluidRegistry.getBucketFluids().contains(fluid))
        {
            UniversalBucket bucket = ForgeModContainer.getInstance().universalBucket;
            ItemStack filledBucket = new ItemStack(bucket);
            FluidStack fluidContents = new FluidStack(fluidStack, bucket.getCapacity());

            NBTTagCompound tag = new NBTTagCompound();
            fluidContents.writeToNBT(tag);
            filledBucket.setTagCompound(tag);

            return filledBucket;
        }

        return null;
    }
}
