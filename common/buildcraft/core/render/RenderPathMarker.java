/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

import buildcraft.core.EntityLaser;
import buildcraft.core.LaserData;
import buildcraft.core.TilePathMarker;

public class RenderPathMarker extends TileEntitySpecialRenderer<TilePathMarker> {
    public RenderPathMarker() {}

    @Override
    public void renderTileEntityAt(TilePathMarker marker, double x, double y, double z, float f, int arg) {
        if (marker != null) {
            GL11.glPushMatrix();
            GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
            GlStateManager.enableCull();
            GlStateManager.enableLighting();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            GL11.glTranslated(x, y, z);
            GL11.glTranslated(-marker.getPos().getX(), -marker.getPos().getY(), -marker.getPos().getZ());

            for (LaserData laser : marker.lasers) {
                if (laser != null) {
                    GL11.glPushMatrix();
                    RenderLaser.doRenderLaser(TileEntityRendererDispatcher.instance.worldObj, Minecraft.getMinecraft().renderEngine, laser,
                            EntityLaser.LASER_BLUE);
                    GL11.glPopMatrix();
                }
            }

            GL11.glPopAttrib();
            GL11.glPopMatrix();
        }
    }

    @Override
    public boolean forceTileEntityRender() {
        return true;
    }
}
