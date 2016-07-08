package buildcraft.lib.fluids;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class TankUtils {
    public static void pushFluidAround(IBlockAccess world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        Tank tank = (Tank) tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
        for(EnumFacing side : EnumFacing.values()) {
            TileEntity tileToPush = world.getTileEntity(pos.offset(side));
            if(tileToPush != null && tileToPush.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null)) {
                IFluidHandler tankToPush = tileToPush.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
                int used = tankToPush.fill(tank.getFluid(), true);

                if(used > 0) {
                    tank.drain(used, true);
                }
            }
        }
    }
    public static void popFluidAround(IBlockAccess world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        Tank tank = (Tank) tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
        for(EnumFacing side : EnumFacing.values()) {
            TileEntity tileToPop = world.getTileEntity(pos.offset(side));
            if(tileToPop != null && tileToPop.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null)) {
                Tank tankToPop = (Tank) tileToPop.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
                if(tankToPop.getFluid() != null) {
                    int used = tank.fill(tankToPop.getFluid(), true);

                    if(used > 0) {
                        tankToPop.drain(used, true);
                    }
                }
            }
        }
    }
}
