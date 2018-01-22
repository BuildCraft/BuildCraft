/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.marker.volume;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;

import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class AddonDefaultRenderer<T extends Addon> implements IFastAddonRenderer<T> {
    private final TextureAtlasSprite s;

    public AddonDefaultRenderer() {
        s = ModelLoader.White.INSTANCE;
    }

    public AddonDefaultRenderer(TextureAtlasSprite s) {
        this.s = s;
    }

    @Override
    public void renderAddonFast(T addon, EntityPlayer player, float partialTicks, VertexBuffer builder) {
        AxisAlignedBB bb = addon.getBoundingBox();

        builder.pos(bb.minX, bb.maxY, bb.minZ).color(204, 204, 204, 255).tex(s.getMinU(), s.getMinV()).lightmap(240, 0).endVertex();
        builder.pos(bb.maxX, bb.maxY, bb.minZ).color(204, 204, 204, 255).tex(s.getMinU(), s.getMaxV()).lightmap(240, 0).endVertex();
        builder.pos(bb.maxX, bb.minY, bb.minZ).color(204, 204, 204, 255).tex(s.getMaxU(), s.getMaxV()).lightmap(240, 0).endVertex();
        builder.pos(bb.minX, bb.minY, bb.minZ).color(204, 204, 204, 255).tex(s.getMaxU(), s.getMinV()).lightmap(240, 0).endVertex();

        builder.pos(bb.minX, bb.minY, bb.maxZ).color(204, 204, 204, 255).tex(s.getMinU(), s.getMinV()).lightmap(240, 0).endVertex();
        builder.pos(bb.maxX, bb.minY, bb.maxZ).color(204, 204, 204, 255).tex(s.getMinU(), s.getMaxV()).lightmap(240, 0).endVertex();
        builder.pos(bb.maxX, bb.maxY, bb.maxZ).color(204, 204, 204, 255).tex(s.getMaxU(), s.getMaxV()).lightmap(240, 0).endVertex();
        builder.pos(bb.minX, bb.maxY, bb.maxZ).color(204, 204, 204, 255).tex(s.getMaxU(), s.getMinV()).lightmap(240, 0).endVertex();

        builder.pos(bb.minX, bb.minY, bb.minZ).color(127, 127, 127, 255).tex(s.getMinU(), s.getMinV()).lightmap(240, 0).endVertex();
        builder.pos(bb.maxX, bb.minY, bb.minZ).color(127, 127, 127, 255).tex(s.getMinU(), s.getMaxV()).lightmap(240, 0).endVertex();
        builder.pos(bb.maxX, bb.minY, bb.maxZ).color(127, 127, 127, 255).tex(s.getMaxU(), s.getMaxV()).lightmap(240, 0).endVertex();
        builder.pos(bb.minX, bb.minY, bb.maxZ).color(127, 127, 127, 255).tex(s.getMaxU(), s.getMinV()).lightmap(240, 0).endVertex();

        builder.pos(bb.minX, bb.maxY, bb.maxZ).color(255, 255, 255, 255).tex(s.getMinU(), s.getMinV()).lightmap(240, 0).endVertex();
        builder.pos(bb.maxX, bb.maxY, bb.maxZ).color(255, 255, 255, 255).tex(s.getMinU(), s.getMaxV()).lightmap(240, 0).endVertex();
        builder.pos(bb.maxX, bb.maxY, bb.minZ).color(255, 255, 255, 255).tex(s.getMaxU(), s.getMaxV()).lightmap(240, 0).endVertex();
        builder.pos(bb.minX, bb.maxY, bb.minZ).color(255, 255, 255, 255).tex(s.getMaxU(), s.getMinV()).lightmap(240, 0).endVertex();

        builder.pos(bb.minX, bb.minY, bb.maxZ).color(153, 153, 153, 255).tex(s.getMinU(), s.getMinV()).lightmap(240, 0).endVertex();
        builder.pos(bb.minX, bb.maxY, bb.maxZ).color(153, 153, 153, 255).tex(s.getMinU(), s.getMaxV()).lightmap(240, 0).endVertex();
        builder.pos(bb.minX, bb.maxY, bb.minZ).color(153, 153, 153, 255).tex(s.getMaxU(), s.getMaxV()).lightmap(240, 0).endVertex();
        builder.pos(bb.minX, bb.minY, bb.minZ).color(153, 153, 153, 255).tex(s.getMaxU(), s.getMinV()).lightmap(240, 0).endVertex();

        builder.pos(bb.maxX, bb.minY, bb.minZ).color(153, 153, 153, 255).tex(s.getMinU(), s.getMinV()).lightmap(240, 0).endVertex();
        builder.pos(bb.maxX, bb.maxY, bb.minZ).color(153, 153, 153, 255).tex(s.getMinU(), s.getMaxV()).lightmap(240, 0).endVertex();
        builder.pos(bb.maxX, bb.maxY, bb.maxZ).color(153, 153, 153, 255).tex(s.getMaxU(), s.getMaxV()).lightmap(240, 0).endVertex();
        builder.pos(bb.maxX, bb.minY, bb.maxZ).color(153, 153, 153, 255).tex(s.getMaxU(), s.getMinV()).lightmap(240, 0).endVertex();
    }
}
