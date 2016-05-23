/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.factory.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fluids.FluidStack;

import buildcraft.core.lib.client.render.FluidRenderer;
import buildcraft.core.lib.client.render.RenderUtils;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.factory.TileTank;

public class RenderTank extends TileEntitySpecialRenderer<TileTank> {
    private static final Vec3d TANK_SIZE = new Vec3d(0.74, 1, 0.74);

    @Override
    public void renderTileEntityAt(TileTank tank, double x, double y, double z, float f, int minusOne) {
        tank = CoreProxy.proxy.getServerTile(tank);

        FluidStack liquid = tank.tank.getFluid();
        if (liquid == null || liquid.getFluid() == null || liquid.amount <= 0) {
            return;
        }

        // Workaround: The colorRenderCache from the server tile from getServerTile(...) does not get synced properly
        int color;
        if (tank.getWorld().isRemote) {
            color = tank.tank.colorRenderCache;
        } else {
            color = liquid.getFluid().getColor(liquid);
        }

        int[] displayList = FluidRenderer.getFluidDisplayLists(liquid, FluidRenderer.FluidType.STILL, TANK_SIZE);
        if (displayList == null) {
            return;
        }

        GL11.glPushMatrix();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        RenderUtils.setGLColorFromIntPlusAlpha(color);

        GL11.glTranslatef((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);
        // float scale = 0.95f;
        // GL11.glScalef(scale, scale, scale);
        GL11.glTranslatef(-0.370F, -0.5F, -0.370F);

        int listIndex = (int) ((float) liquid.amount / (float) (tank.tank.getCapacity()) * (FluidRenderer.DISPLAY_STAGES - 1));

        GL11.glCallList(displayList[listIndex]);

        GlStateManager.enableLighting();
        GlStateManager.disableBlend();

        GL11.glPopMatrix();
    }
}
