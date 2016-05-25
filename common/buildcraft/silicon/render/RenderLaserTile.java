/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.silicon.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;

import buildcraft.core.render.RenderLaser;
import buildcraft.silicon.TileLaser;

public class RenderLaserTile extends TileEntitySpecialRenderer<TileLaser> {

    @Override
    public void renderTileEntityAt(TileLaser laser, double x, double y, double z, float f, int i) {
        Minecraft.getMinecraft().mcProfiler.startSection("bc");
        Minecraft.getMinecraft().mcProfiler.startSection("laser_tile");
        if (laser != null) {
            GL11.glPushMatrix();
            GL11.glTranslated(x, y, z);
            GL11.glTranslated(-laser.getPos().getX(), -laser.getPos().getY(), -laser.getPos().getZ());

            GL11.glPushMatrix();
            GlStateManager.disableLighting();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 0xF0, 0xF0);
            RenderLaser.doRenderLaser(TileEntityRendererDispatcher.instance.worldObj, TileEntityRendererDispatcher.instance.renderEngine, laser.laser,
                    laser.getTexture());
            GlStateManager.enableLighting();
            GL11.glPopMatrix();

            GL11.glPopMatrix();
        }
        Minecraft.getMinecraft().mcProfiler.endSection();
        Minecraft.getMinecraft().mcProfiler.endSection();
    }
    
    @Override
    public boolean forceTileEntityRender() {
        return true;
    }
}
