package buildcraft.test.lib.fluid;

import org.junit.Assert;
import org.junit.Test;

import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import buildcraft.lib.fluid.Tank;
import buildcraft.lib.fluid.TankManager;

import buildcraft.test.VanillaSetupBaseTester;

import java.util.Objects;

public class TankManagerTester extends VanillaSetupBaseTester {
    @Test
    public void testSimpleMoving() {
        TankManager manager = new TankManager();
        manager.add(new Tank("tank_1", 3, null));
        Assert.assertEquals(2, manager.fill(new FluidStack(FluidRegistry.WATER, 2), true));
        Assert.assertEquals(1, manager.fill(new FluidStack(FluidRegistry.WATER, 2), true));
        Assert.assertTrue(new FluidStack(FluidRegistry.WATER, 3).isFluidStackIdentical(manager.drain(new FluidStack(FluidRegistry.WATER, 5), true)));

        manager.add(new Tank("tank_2", 3, null));

        Assert.assertEquals(5, manager.fill(new FluidStack(FluidRegistry.LAVA, 5), true));
        Assert.assertTrue(new FluidStack(FluidRegistry.LAVA, 4).isFluidStackIdentical(manager.drain(new FluidStack(FluidRegistry.LAVA, 4), true)));

        Assert.assertEquals(1, Objects.requireNonNull(manager.get(1).getFluid()).amount);
    }
}
