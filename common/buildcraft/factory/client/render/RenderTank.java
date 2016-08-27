package buildcraft.factory.client.render;

import buildcraft.factory.block.BlockTank;
import buildcraft.factory.tile.TileTank;
import buildcraft.lib.client.render.fluid.FluidRenderer;
import buildcraft.lib.client.render.fluid.FluidSpriteType;
import buildcraft.lib.misc.CullTESR;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;

public class RenderTank extends CullTESR<TileTank> {
    private static final Vec3d MIN_NORMAL = new Vec3d(2 / 16D + 0.01, 0 / 16D + 0.01, 2 / 16D + 0.01);
    private static final Vec3d MIN_CONNECTED = new Vec3d(2 / 16D + 0.01, 0 / 16D, 2 / 16D + 0.01);
    private static final Vec3d MAX = new Vec3d(14 / 16D - 0.01, 16 / 16D, 14 / 16D - 0.01);

    public RenderTank() {}

    @Override
    public void renderTileEntityFast(TileTank tile, double x, double y, double z, float partialTicks, int destroyStage, VertexBuffer buffer) {
        boolean[] sideRender = { true, true, true, true, true, true };
        sideRender[EnumFacing.DOWN.ordinal()] = !(tile.getWorld().getBlockState(tile.getPos().offset(EnumFacing.DOWN)).getBlock() instanceof BlockTank);
        TileEntity upTile = tile.getWorld().getTileEntity(tile.getPos().offset(EnumFacing.UP));
        sideRender[EnumFacing.UP.ordinal()] = !(tile.getWorld().getBlockState(tile.getPos().offset(EnumFacing.UP)).getBlock() instanceof BlockTank && upTile != null && upTile instanceof TileTank && !((TileTank) upTile).tank.isEmpty());
        buffer.setTranslation(x, y, z);
        // TODO: use a DeltaInt for the fluid amount
        FluidRenderer.renderFluid(FluidSpriteType.STILL, tile.tank, sideRender[EnumFacing.DOWN.ordinal()] ? MIN_NORMAL : MIN_CONNECTED, MAX, buffer, sideRender);
    }
}
