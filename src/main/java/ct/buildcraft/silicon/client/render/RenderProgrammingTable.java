/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.silicon.client.render;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;

import ct.buildcraft.silicon.tile.TileProgrammingTable_Neptune;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderProgrammingTable implements BlockEntityRenderer<TileProgrammingTable_Neptune> {
	
	private static TextureAtlasSprite WHITE_STAINED_GLASS_TEX;
	
	public RenderProgrammingTable(BlockEntityRendererProvider.Context bpc) {
		WHITE_STAINED_GLASS_TEX = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(new ResourceLocation("block/white_stained_glass"));
	}
	
    @Override
    public void render(@Nonnull TileProgrammingTable_Neptune tile, float partialTicks, PoseStack matrix, MultiBufferSource buffer, int combinedLight, int overlay) {
        Minecraft.getInstance().getProfiler().push("bc");
        Minecraft.getInstance().getProfiler().push("table");
        Minecraft.getInstance().getProfiler().push("programming");

        int light1 = combinedLight >> 16 & 65535;
        int light2 = combinedLight & 65535;
        VertexConsumer bb = buffer.getBuffer(RenderType.translucent());
        Matrix4f pose = matrix.last().pose();
        Matrix3f normal = matrix.last().normal();
        bb.vertex(4 / 16D, 9 / 16D, 4 / 16D).color(255, 255, 255, 255).uv(WHITE_STAINED_GLASS_TEX.getU(4), WHITE_STAINED_GLASS_TEX.getV(4)).overlayCoords(overlay).uv2(light1, light2).normal(normal, 0, 0, 1).endVertex();
        bb.vertex(2 / 16D, 9 / 16D, 4 / 16D).color(255, 255, 255, 255).uv(WHITE_STAINED_GLASS_TEX.getU(12), WHITE_STAINED_GLASS_TEX.getV(4)).overlayCoords(overlay).uv2(light1, light2).normal(normal, 0, 0, 1).endVertex();
        bb.vertex(12 / 16D, 9 / 16D, 12 / 16D).color(255, 255, 255, 255).uv(WHITE_STAINED_GLASS_TEX.getU(12), WHITE_STAINED_GLASS_TEX.getV(12)).overlayCoords(overlay).uv2(light1, light2).normal(normal, 0, 0, 1).endVertex();
        bb.vertex(4 / 16D, 9 / 16D, 12 / 16D).color(255, 255, 255, 255).uv(WHITE_STAINED_GLASS_TEX.getU(4), WHITE_STAINED_GLASS_TEX.getV(12)).overlayCoords(overlay).uv2(light1, light2).normal(normal, 0, 0, 1).endVertex();

        Minecraft.getInstance().getProfiler().pop();
        Minecraft.getInstance().getProfiler().pop();
        Minecraft.getInstance().getProfiler().pop();
    }
}
