package buildcraft.lib.fluids;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

public class TankUtils {
    public static void pushFluidAround(IBlockAccess world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        Tank tank = (Tank) tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
        for(EnumFacing side : EnumFacing.values()) {
            TileEntity tileToPush = world.getTileEntity(pos.offset(side));
            if(tileToPush != null && tileToPush.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null)) {
                Tank tankToPush = (Tank) tileToPush.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
                int used = tankToPush.fill(tank.getFluid(), true);

                if(used > 0) {
                    tank.drain(used, true);
                }
            }
        }
    }
}
