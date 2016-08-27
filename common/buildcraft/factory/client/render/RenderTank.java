package buildcraft.factory.client.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.util.math.Vec3d;

import net.minecraftforge.client.model.animation.FastTESR;

import buildcraft.factory.tile.TileTank;
import buildcraft.lib.client.render.fluid.FluidRenderer;
import buildcraft.lib.client.render.fluid.FluidSpriteType;

public class RenderTank extends FastTESR<TileTank> {
    private static final Vec3d MIN = new Vec3d(0.13, 0.01, 0.13);
    private static final Vec3d MAX = new Vec3d(0.86, 0.99, 0.86);

    public RenderTank() {}

    @Override
    public void renderTileEntityFast(TileTank tile, double x, double y, double z, float partialTicks, int destroyStage, VertexBuffer buffer) {
        boolean[] sideRender = { true, true, true, true, true, true };
        // sideRender[EnumFacing.DOWN.ordinal()] = false;
        // sideRender[EnumFacing.UP.ordinal()] = false;
        buffer.setTranslation(x, y, z);
        // TODO: use a DeltaInt for the fluid amount
        FluidRenderer.renderFluid(FluidSpriteType.STILL, tile.tank, MIN, MAX, buffer, sideRender);

        // force enable culling -- not using the state manager because fastTESR doesn't do what we want it to
        GL11.glEnable(GL11.GL_CULL_FACE);
    }
}
