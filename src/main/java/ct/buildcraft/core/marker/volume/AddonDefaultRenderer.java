/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.core.marker.volume;

import com.mojang.blaze3d.vertex.BufferBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AddonDefaultRenderer<T extends Addon> implements IFastAddonRenderer<T> {
    private final TextureAtlasSprite s;

    public AddonDefaultRenderer() {
        s = Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS).getSprite(new ResourceLocation("quartz_block_top"));//TODO change to white
    }

    public AddonDefaultRenderer(TextureAtlasSprite s) {
        this.s = s;
    }

    @Override
    public void renderAddonFast(T addon, Player player, float partialTicks, BufferBuilder builder) {
        AABB bb = addon.getBoundingBox();

        builder.vertex(bb.minX, bb.maxY, bb.minZ).color(204, 204, 204, 255).uv(s.getU0(), s.getV0()).uv2(240, 0).endVertex();
        builder.vertex(bb.maxX, bb.maxY, bb.minZ).color(204, 204, 204, 255).uv(s.getU0(), s.getV1()).uv2(240, 0).endVertex();
        builder.vertex(bb.maxX, bb.minY, bb.minZ).color(204, 204, 204, 255).uv(s.getU1(), s.getV1()).uv2(240, 0).endVertex();
        builder.vertex(bb.minX, bb.minY, bb.minZ).color(204, 204, 204, 255).uv(s.getU1(), s.getV0()).uv2(240, 0).endVertex();

        builder.vertex(bb.minX, bb.minY, bb.maxZ).color(204, 204, 204, 255).uv(s.getU0(), s.getV0()).uv2(240, 0).endVertex();
        builder.vertex(bb.maxX, bb.minY, bb.maxZ).color(204, 204, 204, 255).uv(s.getU0(), s.getV1()).uv2(240, 0).endVertex();
        builder.vertex(bb.maxX, bb.maxY, bb.maxZ).color(204, 204, 204, 255).uv(s.getU1(), s.getV1()).uv2(240, 0).endVertex();
        builder.vertex(bb.minX, bb.maxY, bb.maxZ).color(204, 204, 204, 255).uv(s.getU1(), s.getV0()).uv2(240, 0).endVertex();

        builder.vertex(bb.minX, bb.minY, bb.minZ).color(127, 127, 127, 255).uv(s.getU0(), s.getV0()).uv2(240, 0).endVertex();
        builder.vertex(bb.maxX, bb.minY, bb.minZ).color(127, 127, 127, 255).uv(s.getU0(), s.getV1()).uv2(240, 0).endVertex();
        builder.vertex(bb.maxX, bb.minY, bb.maxZ).color(127, 127, 127, 255).uv(s.getU1(), s.getV1()).uv2(240, 0).endVertex();
        builder.vertex(bb.minX, bb.minY, bb.maxZ).color(127, 127, 127, 255).uv(s.getU1(), s.getV0()).uv2(240, 0).endVertex();

        builder.vertex(bb.minX, bb.maxY, bb.maxZ).color(255, 255, 255, 255).uv(s.getU0(), s.getV0()).uv2(240, 0).endVertex();
        builder.vertex(bb.maxX, bb.maxY, bb.maxZ).color(255, 255, 255, 255).uv(s.getU0(), s.getV1()).uv2(240, 0).endVertex();
        builder.vertex(bb.maxX, bb.maxY, bb.minZ).color(255, 255, 255, 255).uv(s.getU1(), s.getV1()).uv2(240, 0).endVertex();
        builder.vertex(bb.minX, bb.maxY, bb.minZ).color(255, 255, 255, 255).uv(s.getU1(), s.getV0()).uv2(240, 0).endVertex();

        builder.vertex(bb.minX, bb.minY, bb.maxZ).color(153, 153, 153, 255).uv(s.getU0(), s.getV0()).uv2(240, 0).endVertex();
        builder.vertex(bb.minX, bb.maxY, bb.maxZ).color(153, 153, 153, 255).uv(s.getU0(), s.getV1()).uv2(240, 0).endVertex();
        builder.vertex(bb.minX, bb.maxY, bb.minZ).color(153, 153, 153, 255).uv(s.getU1(), s.getV1()).uv2(240, 0).endVertex();
        builder.vertex(bb.minX, bb.minY, bb.minZ).color(153, 153, 153, 255).uv(s.getU1(), s.getV0()).uv2(240, 0).endVertex();

        builder.vertex(bb.maxX, bb.minY, bb.minZ).color(153, 153, 153, 255).uv(s.getU0(), s.getV0()).uv2(240, 0).endVertex();
        builder.vertex(bb.maxX, bb.maxY, bb.minZ).color(153, 153, 153, 255).uv(s.getU0(), s.getV1()).uv2(240, 0).endVertex();
        builder.vertex(bb.maxX, bb.maxY, bb.maxZ).color(153, 153, 153, 255).uv(s.getU1(), s.getV1()).uv2(240, 0).endVertex();
        builder.vertex(bb.maxX, bb.minY, bb.maxZ).color(153, 153, 153, 255).uv(s.getU1(), s.getV0()).uv2(240, 0).endVertex();
    }
}
