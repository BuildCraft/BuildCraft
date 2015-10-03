/** Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.tileentity.TileEntity;

import buildcraft.api.core.BCLog;
import buildcraft.core.StackAtPosition;
import buildcraft.core.builders.BuildingItem;
import buildcraft.core.builders.IBuildingItemsProvider;

public class RenderBuildingItems {

    private final RenderItem renderItem;

    public RenderBuildingItems() {
        renderItem = Minecraft.getMinecraft().getRenderItem();
    }

    public void render(TileEntity tile, double x, double y, double z) {

        IBuildingItemsProvider provider = (IBuildingItemsProvider) tile;
        GL11.glPushMatrix();

        GL11.glTranslated(x, y, z);
        GL11.glTranslated(-tile.getPos().getX(), -tile.getPos().getY(), -tile.getPos().getZ());

        if (provider.getBuilders() != null) {
            synchronized (provider.getBuilders()) {
                for (BuildingItem i : provider.getBuilders()) {
                    doRenderItem(i, 1.0F);
                }
            }
        }

        GL11.glPopMatrix();
    }

    private void doRenderItem(BuildingItem i, float light) {
        if (i == null) {
            return;
        }

        i.displayUpdate();

        for (StackAtPosition s : i.getStacks()) {
            if (s.display) {
                float renderScale = 0.7f;
                GL11.glPushMatrix();
                GL11.glTranslatef((float) s.pos.xCoord, (float) s.pos.yCoord, (float) s.pos.zCoord);
                GL11.glTranslatef(0, 0.25F, 0);
                GL11.glScalef(renderScale, renderScale, renderScale);
                if (s.stack != null) {
                    @SuppressWarnings("deprecation")
                    IBakedModel model = renderItem.getItemModelMesher().getItemModel(s.stack);
                    if (model != null) {
                        renderItem.renderItemModel(s.stack);
                    } else {
                        BCLog.logger.warn("Model was null for " + s.stack);
                    }
                } else {
                    BCLog.logger.warn("ItemStack was null for " + s + ", " + i);
                }

                GL11.glPopMatrix();
            }
        }
    }

}
