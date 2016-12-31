package buildcraft.lib.fluids;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import buildcraft.lib.misc.CapUtil;

public class TankUtils {
    public static void pushFluidAround(IBlockAccess world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        Tank tank = (Tank) tile.getCapability(CapUtil.CAP_FLUIDS, null);
        for(EnumFacing side : EnumFacing.values()) {
            TileEntity tileToPush = world.getTileEntity(pos.offset(side));
            if(tileToPush != null && tileToPush.hasCapability(CapUtil.CAP_FLUIDS, null)) {
                IFluidHandler tankToPush = tileToPush.getCapability(CapUtil.CAP_FLUIDS, null);
                int used = tankToPush.fill(tank.getFluid(), true);

                if(used > 0) {
                    tank.drain(used, true);
                }
            }
        }
    }
    public static void pullFluidAround(IBlockAccess world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        Tank tank = (Tank) tile.getCapability(CapUtil.CAP_FLUIDS, null);
        for(EnumFacing side : EnumFacing.values()) {
            TileEntity tileToPop = world.getTileEntity(pos.offset(side));
            if(tileToPop != null && tileToPop.hasCapability(CapUtil.CAP_FLUIDS, null)) {
                IFluidHandler tankToPull = tileToPop.getCapability(CapUtil.CAP_FLUIDS, null);
                FluidStack fluidStack = tankToPull.drain(1000, false);
                if(fluidStack != null && fluidStack.amount != 0) {
                    int used = tank.fill(fluidStack, true);

                    if(used > 0) {
                        tankToPull.drain(used, true);
                    }
                }
            }
        }
    }
}
