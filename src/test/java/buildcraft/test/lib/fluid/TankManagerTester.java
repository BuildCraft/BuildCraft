package buildcraft.test.lib.fluid;

import buildcraft.lib.fluid.Tank;
import buildcraft.lib.fluid.TankManager;
import buildcraft.test.VanillaSetupBaseTester;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.junit.Assert;
import org.junit.Test;

public class TankManagerTester extends VanillaSetupBaseTester {
    @Test
    public void testSimpleMoving() {
        TankManager manager = new TankManager();
        manager.add(new Tank("tank_1", 3, null));
//        Assert.assertEquals(2, manager.fill(new FluidStack(FluidRegistry.WATER, 2), true));
        Assert.assertEquals(2, manager.fill(new FluidStack(Fluids.WATER, 2), IFluidHandler.FluidAction.EXECUTE));
//        Assert.assertEquals(1, manager.fill(new FluidStack(FluidRegistry.WATER, 2), true));
        Assert.assertEquals(1, manager.fill(new FluidStack(Fluids.WATER, 2), IFluidHandler.FluidAction.EXECUTE));
//        Assert.assertTrue(new FluidStack(FluidRegistry.WATER, 3).isFluidStackIdentical(manager.drain(new FluidStack(FluidRegistry.WATER, 5), true)));
        Assert.assertTrue(new FluidStack(Fluids.WATER, 3).isFluidStackIdentical(manager.drain(new FluidStack(Fluids.WATER, 5), IFluidHandler.FluidAction.EXECUTE)));

        manager.add(new Tank("tank_2", 3, null));

//        Assert.assertEquals(5, manager.fill(new FluidStack(FluidRegistry.LAVA, 5), true));
        Assert.assertEquals(5, manager.fill(new FluidStack(Fluids.LAVA, 5), IFluidHandler.FluidAction.EXECUTE));
//        Assert.assertTrue(new FluidStack(FluidRegistry.LAVA, 4).isFluidStackIdentical(manager.drain(new FluidStack(FluidRegistry.LAVA, 4), true)));
        Assert.assertTrue(new FluidStack(Fluids.LAVA, 4).isFluidStackIdentical(manager.drain(new FluidStack(Fluids.LAVA, 4), IFluidHandler.FluidAction.EXECUTE)));

//        Assert.assertEquals(1, manager.get(1).getFluid().amount);
        Assert.assertEquals(1, manager.get(1).getFluid().getAmount());
    }
}
