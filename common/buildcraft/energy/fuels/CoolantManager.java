/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.energy.fuels;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import buildcraft.api.core.StackKey;
import buildcraft.api.fuels.ICoolant;
import buildcraft.api.fuels.ICoolantManager;
import buildcraft.api.fuels.ISolidCoolant;

public final class CoolantManager implements ICoolantManager {
	public static final CoolantManager INSTANCE = new CoolantManager();

	private final List<ICoolant> coolants = new LinkedList<ICoolant>();
	private final List<ISolidCoolant> solidCoolants = new LinkedList<ISolidCoolant>();

	private CoolantManager() {
	}

	@Override
	public ICoolant addCoolant(ICoolant coolant) {
		coolants.add(coolant);
		return coolant;
	}

	@Override
	public ICoolant addCoolant(Fluid fluid, float degreesCoolingPerMB) {
		return addCoolant(new BCCoolant(fluid, degreesCoolingPerMB));
	}

	@Override
	public ISolidCoolant addSolidCoolant(ISolidCoolant solidCoolant) {
		solidCoolants.add(solidCoolant);
		return solidCoolant;
	}

	@Override
	public ISolidCoolant addSolidCoolant(StackKey solid, StackKey liquid, float multiplier) {
		assert solid.stack != null && solid.fluidStack == null;
		assert liquid.stack == null && liquid.fluidStack != null;
		return addSolidCoolant(new BCSolidCoolant(solid, liquid, multiplier));
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
	public ISolidCoolant getSolidCoolant(StackKey solid) {
		assert solid.stack != null && solid.fluidStack == null;
		for (ISolidCoolant solidCoolant : solidCoolants) {
			if (solidCoolant.getFluidFromSolidCoolant(solid.stack) != null) {
				return solidCoolant;
			}
		}
		return null;
	}

	private static final class BCCoolant implements ICoolant {
		private final Fluid fluid;
		private final float degreesCoolingPerMB;

		public BCCoolant(Fluid fluid, float degreesCoolingPerMB) {
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

	private static final class BCSolidCoolant implements ISolidCoolant {
		private final StackKey solid;
		private final StackKey liquid;
		private final float multiplier;

		public BCSolidCoolant(StackKey solid, StackKey liquid, float multiplier) {
			this.solid = solid;
			this.liquid = liquid;
			this.multiplier = multiplier;
		}

		@Override
		public FluidStack getFluidFromSolidCoolant(ItemStack stack) {
			if (stack == null || !stack.isItemEqual(solid.stack)) {
				return null;
			}
			int liquidAmount = (int) (stack.stackSize * liquid.fluidStack.amount * multiplier / solid.stack.stackSize);
			return new FluidStack(liquid.fluidStack.getFluid(), liquidAmount);
		}
	}
}
