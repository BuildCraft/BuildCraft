/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.factory.client.render;

import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.api.properties.BuildCraftProperty;
import buildcraft.core.lib.utils.MathUtils;
import buildcraft.factory.tile.ITileLed;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.properties.IProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.common.property.Properties;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class RenderLeds<T extends TileEntity & ITileLed> extends TileEntitySpecialRenderer<T> {
    public static final RenderLeds INSTANCE = new RenderLeds();

    @Override
    public void renderTileEntityAt(T tile, double x, double y, double z, float partialTicks, int destroyStage) {
        RenderHelper.disableStandardItemLighting();
        GL11.glColor3f(1, 1, 1);
        int i = 61680;
        int j = i % 65536;
        int k = i / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j, (float)k);
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
        ModelBase model = new ModelBase() {
            @Override
            public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
                if(tile.isDone()) {
                    bindTexture(new ResourceLocation("buildcraftcore", "textures/blocks/led_green_off.png"));
                } else {
                    bindTexture(new ResourceLocation("buildcraftcore", "textures/blocks/led_green_on.png"));
                }
                new ModelRenderer(this).addBox(tile.getX(), tile.getY(), -0.1F, 1, 1, 1).render(scale);
                bindTexture(new ResourceLocation("buildcraftcore", "textures/blocks/led_red_" + MathUtils.clamp(tile.getPowerLevel(), 0, 3) + "_3.png"));
                new ModelRenderer(this).addBox(tile.getX() - 2, tile.getY(), -0.1F, 1, 1, 1).render(scale);
            }
        };
        model.render(null, 0, 0, 0, 0, 0, 0.0625F);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();
        RenderHelper.enableStandardItemLighting();
    }
}
