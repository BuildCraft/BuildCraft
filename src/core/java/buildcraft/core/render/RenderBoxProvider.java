/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import buildcraft.core.Box;
import buildcraft.core.EntityLaser;
import buildcraft.core.internal.IBoxProvider;
import buildcraft.core.internal.IBoxesProvider;

public class RenderBoxProvider extends TileEntitySpecialRenderer {
    public RenderBoxProvider() {}

    @Override
    public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float f, int anArgument) {
        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glPushMatrix();
        GL11.glTranslated(-tileentity.getPos().getX(), -tileentity.getPos().getY(), -tileentity.getPos().getZ());
        GL11.glTranslated(x, y, z);

        if (tileentity instanceof IBoxesProvider) {
            for (Box b : ((IBoxesProvider) tileentity).getBoxes()) {
                if (b.isVisible) {
                    RenderBox.doRender(TileEntityRendererDispatcher.instance.worldObj, Minecraft.getMinecraft().renderEngine, getTexture(b.kind), b);
                }
            }
        } else if (tileentity instanceof IBoxProvider) {
            Box b = ((IBoxProvider) tileentity).getBox();

            if (b.isVisible) {
                RenderBox.doRender(TileEntityRendererDispatcher.instance.worldObj, Minecraft.getMinecraft().renderEngine, getTexture(b.kind), b);
            }
        }

        GL11.glPopMatrix();
        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }

    private ResourceLocation getTexture(Box.Kind kind) {
        switch (kind) {
            case LASER_RED:
                return EntityLaser.LASER_RED;
            case LASER_YELLOW:
                return EntityLaser.LASER_YELLOW;
            case LASER_GREEN:
                return EntityLaser.LASER_GREEN;
            case LASER_BLUE:
                return EntityLaser.LASER_BLUE;
            case STRIPES:
                return EntityLaser.LASER_STRIPES_YELLOW;
            case BLUE_STRIPES:
                return EntityLaser.LASER_STRIPES_BLUE;
        }

        return null;
    }
}
