package buildcraft.lib.fluid;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import buildcraft.api.fuels.ICoolant;
import buildcraft.api.fuels.ICoolantManager;
import buildcraft.api.fuels.ISolidCoolant;

public enum CoolantRegistry implements ICoolantManager {
    INSTANCE;

    private final List<ICoolant> coolants = new LinkedList<>();
    private final List<ISolidCoolant> solidCoolants = new LinkedList<>();

    @Override
    public ICoolant addCoolant(ICoolant coolant) {
        coolants.add(coolant);
        return coolant;
    }

    @Override
    public ISolidCoolant addSolidCoolant(ISolidCoolant solidCoolant) {
        solidCoolants.add(solidCoolant);
        return solidCoolant;
    }

    @Override
    public ICoolant addCoolant(Fluid fluid, float degreesCoolingPerMB) {
        return addCoolant(new Coolant(fluid, degreesCoolingPerMB));
    }

    @Override
    public ISolidCoolant addSolidCoolant(ItemStack solid, FluidStack fluid, float multiplier) {
        return addSolidCoolant(new SolidCoolant(solid, fluid, multiplier));
    }

    @Override
    public Collection<ICoolant> getCoolants() {
        return coolants;
    }

    @Override
    public Collection<ISolidCoolant> getSolidCoolants() {
        return solidCoolants;
    }

    @Override
    public ICoolant getCoolant(Fluid fluid) {
        for (ICoolant coolant : coolants) {
            if (coolant.getFluid() == fluid) {
                return coolant;
            }
        }
        return null;
    }

    @Override
    public ISolidCoolant getSolidCoolant(ItemStack solid) {
        for (ISolidCoolant coolant : solidCoolants) {
            if (coolant.getFluidFromSolidCoolant(solid) != null) {
                return coolant;
            }
        }
        return null;
    }

    private static class Coolant implements ICoolant {
        private final Fluid fluid;
        private final float degreesCoolingPerMB;

        public Coolant(Fluid fluid, float degreesCoolingPerMB) {
            this.fluid = fluid;
            this.degreesCoolingPerMB = degreesCoolingPerMB;
        }

        @Override
        public Fluid getFluid() {
            return fluid;
        }

        @Override
        public float getDegreesCoolingPerMB(float heat) {
            return degreesCoolingPerMB;
        }
    }

    private static class SolidCoolant implements ISolidCoolant {
        private final ItemStack solid;
        private final FluidStack fluid;
        private final float multiplier;

        public SolidCoolant(ItemStack solid, FluidStack fluid, float multiplier) {
            this.solid = solid;
            this.fluid = fluid;
            this.multiplier = multiplier;
        }

        @Override
        public FluidStack getFluidFromSolidCoolant(ItemStack stack) {
            if (stack == null || !stack.isItemEqual(solid)) {
                return null;
            }
            int liquidAmount = (int) (stack.getCount() * fluid.amount * multiplier / solid.getCount());
            return new FluidStack(fluid.getFluid(), liquidAmount);
        }
    }
}
