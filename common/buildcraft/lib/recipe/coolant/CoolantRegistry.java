/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.recipe.coolant;

import buildcraft.api.fuels.*;
import buildcraft.lib.misc.StackUtil;
import com.google.common.collect.Lists;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public enum CoolantRegistry implements ICoolantManager {
    INSTANCE;

    // private final List<IFluidCoolant> coolants = new LinkedList<>();
    private final List<IFluidCoolant> unregisteredFluidCoolants = new LinkedList<>();
    // private final List<ISolidCoolant> solidCoolants = new LinkedList<>();
    private final List<ISolidCoolant> unregisteredSolidCoolants = new LinkedList<>();

    @Override
//    public IFluidCoolant addCoolant(IFluidCoolant coolant)
    public IFluidCoolant addUnregisteredFluidCoolant(IFluidCoolant coolant) {
//        coolants.add(coolant);
        unregisteredFluidCoolants.add(coolant);
        return coolant;
    }

    @Override
//    public ISolidCoolant addSolidCoolant(ISolidCoolant solidCoolant)
    public ISolidCoolant addUnregisteredSolidCoolant(ISolidCoolant solidCoolant) {
//        solidCoolants.add(solidCoolant);
        unregisteredSolidCoolants.add(solidCoolant);
        return solidCoolant;
    }

    @Override
//    public IFluidCoolant addCoolant(FluidStack fluid, float degreesCoolingPerMB)
    public IFluidCoolant addCoolant(ResourceLocation id, FluidStack fluid, float degreesCoolingPerMB) {
//        return addCoolant(new FluidCoolant(fluid, degreesCoolingPerMB));
        return addUnregisteredFluidCoolant(new FluidCoolant(id, fluid, degreesCoolingPerMB));
    }

    @Override
//    public ISolidCoolant addSolidCoolant(ItemStack solid, FluidStack fluid, float multiplier)
    public ISolidCoolant addSolidCoolant(ResourceLocation id, ItemStack solid, FluidStack fluid, float multiplier) {
//        return addSolidCoolant(new SolidCoolant(solid, fluid, multiplier));
        return addUnregisteredSolidCoolant(new SolidCoolant(id, solid, fluid, multiplier));
    }

    @Override
//    public Collection<IFluidCoolant> getCoolants()
    public Collection<IFluidCoolant> getCoolants(Level world) {
//        return coolants;
        Collection<IFluidCoolant> ret = Lists.newArrayList();
        ret.addAll(unregisteredFluidCoolants);
        world.getRecipeManager().byType(ICoolant.TYPE).values().stream().filter(c -> c instanceof IFluidCoolant).forEach(c -> ret.add((IFluidCoolant) c));
        return ret;
    }

    @Override
//    public Collection<ISolidCoolant> getSolidCoolants()
    public Collection<ISolidCoolant> getSolidCoolants(Level world) {
//        return solidCoolants;
        Collection<ISolidCoolant> ret = Lists.newArrayList();
        ret.addAll(unregisteredSolidCoolants);
        world.getRecipeManager().byType(ICoolant.TYPE).values().stream().filter(c -> c instanceof ISolidCoolant).forEach(c -> ret.add((ISolidCoolant) c));
        return ret;
    }

    @Override
//    public IFluidCoolant getCoolant(FluidStack fluid)
    public IFluidCoolant getCoolant(Level world, FluidStack fluid) {
        if (fluid == null || fluid.getAmount() == 0) {
            return null;
        }
//        for (IFluidCoolant coolant : coolants)
        for (IFluidCoolant coolant : getCoolants(world)) {
            if (coolant.matchesFluid(fluid)) {
                return coolant;
            }
        }
        return null;
    }

    @Override
//    public float getDegreesPerMb(FluidStack fluid, float heat)
    public float getDegreesPerMb(Level world, FluidStack fluid, float heat) {
        if (fluid == null || fluid.getAmount() == 0) {
            return 0;
        }
//        for (IFluidCoolant coolant : coolants)
        for (IFluidCoolant coolant : getCoolants(world)) {
            float degrees = coolant.getDegreesCoolingPerMB(fluid, heat);
            if (degrees > 0) {
                return degrees;
            }
        }
        return 0;
    }

    @Override
//    public ISolidCoolant getSolidCoolant(ItemStack solid)
    public ISolidCoolant getSolidCoolant(Level world, ItemStack solid) {
//        for (ISolidCoolant coolant : solidCoolants)
        for (ISolidCoolant coolant : getSolidCoolants(world)) {
            if (coolant.getFluidFromSolidCoolant(solid) != null) {
                return coolant;
            }
        }
        return null;
    }

    // public static class Coolant implements IFluidCoolant
    public static class FluidCoolant implements IFluidCoolant {
        private final FluidStack fluid;
        private final float degreesCoolingPerMB;

        private final ResourceLocation id;

        // public Coolant(FluidStack fluid, float degreesCoolingPerMB)
        public FluidCoolant(ResourceLocation id, FluidStack fluid, float degreesCoolingPerMB) {
            this.fluid = fluid;
            this.degreesCoolingPerMB = degreesCoolingPerMB;
            this.id = id;
        }

        @Override
        public EnumCoolantType getCoolantType() {
            return EnumCoolantType.FLUID;
        }

        @Override
        public boolean matchesFluid(FluidStack stack) {
            return fluid.isFluidEqual(stack);
        }

        @Override
        public float getDegreesCoolingPerMB(FluidStack stack, float heat) {
            if (matchesFluid(stack)) {
                return degreesCoolingPerMB;
            }
            return 0;
        }

        // Calen
        @Override
        public float getDegreesCoolingPerMB() {
            return degreesCoolingPerMB;
        }

        @Override
        public FluidStack getFluid() {
            return fluid;
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public RecipeSerializer<ICoolant> getSerializer() {
            return CoolantRecipeSerializer.INSTANCE;
        }
    }

    // private static class SolidCoolant implements ISolidCoolant
    public static class SolidCoolant implements ISolidCoolant {
        private final ItemStack solid;
        private final FluidStack fluid;
        private final float multiplier;

        private final ResourceLocation id;

        // public SolidCoolant(ItemStack solid, FluidStack fluid, float multiplier)
        public SolidCoolant(ResourceLocation id, ItemStack solid, FluidStack fluid, float multiplier) {
            this.solid = solid;
            this.fluid = fluid;
            this.multiplier = multiplier;
            this.id = id;
        }

        @Override
        public EnumCoolantType getCoolantType() {
            return EnumCoolantType.SOLID;
        }

        @Override
        public FluidStack getFluidFromSolidCoolant(ItemStack stack) {
//            if (stack == null || !stack.isItemEqual(solid))
            if (stack == null || !StackUtil.isSameItemSameDamage(stack, solid)) {
                return null;
            }
            int liquidAmount = (int) (stack.getCount() * fluid.getAmount() * multiplier / solid.getCount());
            return new FluidStack(fluid.getRawFluid(), liquidAmount);
        }

        @Override
        public FluidStack getFluid() {
            return fluid;
        }

        @Override
        public ItemStack getSolid() {
            return solid;
        }

        @Override
        public float getMultiplier() {
            return multiplier;
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public RecipeSerializer<ICoolant> getSerializer() {
            return CoolantRecipeSerializer.INSTANCE;
        }
    }
}
