package buildcraft.lib.fluids;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class TankUtils {
    public static void pushFluidAround(IBlockAccess world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        IHasTank tileWithTank = (IHasTank) tile;
        for(EnumFacing side : EnumFacing.values()) {
            TileEntity tileToPush = world.getTileEntity(pos.offset(side));
            if(tileToPush != null && tileToPush instanceof IHasTank) {
                IHasTank tileToPushWithTank = (IHasTank) tileToPush;
                int used = tileToPushWithTank.getTank().fill(tileWithTank.getTank().getFluid(), true);

                if(used > 0) {
                    tileWithTank.getTank().drain(used, true);
                }
            }
        }
    }
}
