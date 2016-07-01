/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.factory.client.render;

import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.core.lib.utils.MathUtils;
import buildcraft.factory.tile.ITileLed;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.properties.IProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class RenderLeds<T extends TileEntity & ITileLed> extends TileEntitySpecialRenderer<T> {
    public static final RenderLeds INSTANCE = new RenderLeds();

    private ModelLeds model;

    public RenderLeds() {
        model = new ModelLeds();
    }

    @Override
    public void renderTileEntityAt(T tile, double x, double y, double z, float partialTicks, int destroyStage) {
        RenderHelper.disableStandardItemLighting();
        GL11.glColor3f(1, 1, 1);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 0xFF, 0);
        GL11.glPushMatrix();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glTranslated(x, y, z);
        ImmutableMap<IProperty<?>, Comparable<?>> properties = getWorld().getBlockState(tile.getPos()).getProperties();
        if(properties.containsKey(BuildCraftProperties.BLOCK_FACING)) {
            GL11.glTranslatef(0.5F, 0.5F, 0.5F);
            GL11.glRotatef(180 - EnumFacing.valueOf(properties.get(BuildCraftProperties.BLOCK_FACING).toString().toUpperCase()).getHorizontalAngle(), 0, 1, 0);
            GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        }
        model.render(tile);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();
        RenderHelper.enableStandardItemLighting();
    }
}

class ModelLeds<T extends TileEntity & ITileLed> extends ModelBase {
    public void render(T tile) {
        if(tile.isDone()) {
            Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("buildcraftcore", "textures/blocks/led_green_off.png"));
        } else {
            Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("buildcraftcore", "textures/blocks/led_green_on.png"));
        }
        new ModelRenderer(this).addBox(tile.getLedsX(), tile.getLedsY(), -0.1F, 1, 1, 1).render(0.0625F);
        Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("buildcraftcore", "textures/blocks/led_red_" + MathUtils.clamp(tile.getPowerLevel(), 0, 3) + "_3.png"));
        new ModelRenderer(this).addBox(tile.getLedsX() - 2, tile.getLedsY(), -0.1F, 1, 1, 1).render(0.0625F);
    }
}
