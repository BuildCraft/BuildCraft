package buildcraft.core.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

import buildcraft.core.EntityLaser;
import buildcraft.core.LaserData;
import buildcraft.core.TileMarker;

public class RenderMarker extends TileEntitySpecialRenderer<TileMarker> {
    @Override
    public void renderTileEntityAt(TileMarker marker, double x, double y, double z, float partialTicks, int destroyStage) {
        if (marker != null) {

            Minecraft.getMinecraft().mcProfiler.startSection("bc");
            Minecraft.getMinecraft().mcProfiler.startSection("marker");
            GL11.glPushMatrix();
            GL11.glPushAttrib(GL11.GL_ENABLE_BIT);

            GL11.glTranslated(x, y, z);
            GL11.glTranslated(-marker.getPos().getX(), -marker.getPos().getY(), -marker.getPos().getZ());

            Minecraft.getMinecraft().mcProfiler.startSection("laser");

            if (marker.lasers != null) for (LaserData laser : marker.lasers) {
                if (laser != null) {
                    GL11.glPushMatrix();
                    RenderLaser.doRenderLaser(TileEntityRendererDispatcher.instance.worldObj, Minecraft.getMinecraft().renderEngine, laser,
                            EntityLaser.LASER_RED);
                    GL11.glPopMatrix();
                }
            }

            Minecraft.getMinecraft().mcProfiler.endStartSection("signal");

            if (marker.signals != null) for (LaserData laser : marker.signals) {
                if (laser != null) {
                    GL11.glPushMatrix();
                    RenderLaser.doRenderLaser(TileEntityRendererDispatcher.instance.worldObj, Minecraft.getMinecraft().renderEngine, laser,
                            EntityLaser.LASER_BLUE);
                    GL11.glPopMatrix();
                }
            }
            Minecraft.getMinecraft().mcProfiler.endSection();

            GL11.glPopAttrib();
            GL11.glPopMatrix();

            Minecraft.getMinecraft().mcProfiler.endSection();
            Minecraft.getMinecraft().mcProfiler.endSection();
        }
    }

    @Override
    public boolean forceTileEntityRender() {
        return true;
    }
}
