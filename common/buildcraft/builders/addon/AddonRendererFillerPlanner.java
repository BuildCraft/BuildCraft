/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.addon;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import net.minecraftforge.client.model.ModelLoader;

import buildcraft.core.marker.volume.IFastAddonRenderer;

public class AddonRendererFillerPlanner implements IFastAddonRenderer<AddonFillerPlanner> {
    @Override
    public void renderAddonFast(AddonFillerPlanner addon, EntityPlayer player, float partialTicks, BufferBuilder vb) {
        if (addon.buildingInfo == null) {
            return;
        }
        Minecraft.getMinecraft().mcProfiler.startSection("filler_planner");

        Minecraft.getMinecraft().mcProfiler.startSection("iter");
        List<BlockPos> list = StreamSupport.stream(
            BlockPos.getAllInBoxMutable(addon.buildingInfo.box.min(), addon.buildingInfo.box.max()).spliterator(),
            false
        )
            .filter(blockPos ->
                addon.buildingInfo.getSnapshot().data.get(
                    addon.buildingInfo.getSnapshot().posToIndex(
                        addon.buildingInfo.fromWorld(blockPos)
                    )
                )
            )
            .filter(player.world::isAirBlock)
            .map(BlockPos.MutableBlockPos::toImmutable)
            .collect(Collectors.toCollection(ArrayList::new));
        Minecraft.getMinecraft().mcProfiler.endSection();

        Minecraft.getMinecraft().mcProfiler.startSection("sort");
        list.sort(Comparator.<BlockPos>comparingDouble(p -> player.getPositionVector().squareDistanceTo(new Vec3d(p))).reversed());
        Minecraft.getMinecraft().mcProfiler.endSection();

        Minecraft.getMinecraft().mcProfiler.startSection("render");
        for (BlockPos p : list) {
            AxisAlignedBB bb = new AxisAlignedBB(p, p.add(1, 1, 1)).grow(-0.1);
            TextureAtlasSprite s = ModelLoader.White.INSTANCE;

            vb.pos(bb.minX, bb.maxY, bb.minZ).color(204, 204, 204, 127).tex(s.getMinU(), s.getMinV()).lightmap(240, 0).endVertex();
            vb.pos(bb.maxX, bb.maxY, bb.minZ).color(204, 204, 204, 127).tex(s.getMinU(), s.getMaxV()).lightmap(240, 0).endVertex();
            vb.pos(bb.maxX, bb.minY, bb.minZ).color(204, 204, 204, 127).tex(s.getMaxU(), s.getMaxV()).lightmap(240, 0).endVertex();
            vb.pos(bb.minX, bb.minY, bb.minZ).color(204, 204, 204, 127).tex(s.getMaxU(), s.getMinV()).lightmap(240, 0).endVertex();

            vb.pos(bb.minX, bb.minY, bb.maxZ).color(204, 204, 204, 127).tex(s.getMinU(), s.getMinV()).lightmap(240, 0).endVertex();
            vb.pos(bb.maxX, bb.minY, bb.maxZ).color(204, 204, 204, 127).tex(s.getMinU(), s.getMaxV()).lightmap(240, 0).endVertex();
            vb.pos(bb.maxX, bb.maxY, bb.maxZ).color(204, 204, 204, 127).tex(s.getMaxU(), s.getMaxV()).lightmap(240, 0).endVertex();
            vb.pos(bb.minX, bb.maxY, bb.maxZ).color(204, 204, 204, 127).tex(s.getMaxU(), s.getMinV()).lightmap(240, 0).endVertex();

            vb.pos(bb.minX, bb.minY, bb.minZ).color(127, 127, 127, 127).tex(s.getMinU(), s.getMinV()).lightmap(240, 0).endVertex();
            vb.pos(bb.maxX, bb.minY, bb.minZ).color(127, 127, 127, 127).tex(s.getMinU(), s.getMaxV()).lightmap(240, 0).endVertex();
            vb.pos(bb.maxX, bb.minY, bb.maxZ).color(127, 127, 127, 127).tex(s.getMaxU(), s.getMaxV()).lightmap(240, 0).endVertex();
            vb.pos(bb.minX, bb.minY, bb.maxZ).color(127, 127, 127, 127).tex(s.getMaxU(), s.getMinV()).lightmap(240, 0).endVertex();

            vb.pos(bb.minX, bb.maxY, bb.maxZ).color(255, 255, 255, 127).tex(s.getMinU(), s.getMinV()).lightmap(240, 0).endVertex();
            vb.pos(bb.maxX, bb.maxY, bb.maxZ).color(255, 255, 255, 127).tex(s.getMinU(), s.getMaxV()).lightmap(240, 0).endVertex();
            vb.pos(bb.maxX, bb.maxY, bb.minZ).color(255, 255, 255, 127).tex(s.getMaxU(), s.getMaxV()).lightmap(240, 0).endVertex();
            vb.pos(bb.minX, bb.maxY, bb.minZ).color(255, 255, 255, 127).tex(s.getMaxU(), s.getMinV()).lightmap(240, 0).endVertex();

            vb.pos(bb.minX, bb.minY, bb.maxZ).color(153, 153, 153, 127).tex(s.getMinU(), s.getMinV()).lightmap(240, 0).endVertex();
            vb.pos(bb.minX, bb.maxY, bb.maxZ).color(153, 153, 153, 127).tex(s.getMinU(), s.getMaxV()).lightmap(240, 0).endVertex();
            vb.pos(bb.minX, bb.maxY, bb.minZ).color(153, 153, 153, 127).tex(s.getMaxU(), s.getMaxV()).lightmap(240, 0).endVertex();
            vb.pos(bb.minX, bb.minY, bb.minZ).color(153, 153, 153, 127).tex(s.getMaxU(), s.getMinV()).lightmap(240, 0).endVertex();

            vb.pos(bb.maxX, bb.minY, bb.minZ).color(153, 153, 153, 127).tex(s.getMinU(), s.getMinV()).lightmap(240, 0).endVertex();
            vb.pos(bb.maxX, bb.maxY, bb.minZ).color(153, 153, 153, 127).tex(s.getMinU(), s.getMaxV()).lightmap(240, 0).endVertex();
            vb.pos(bb.maxX, bb.maxY, bb.maxZ).color(153, 153, 153, 127).tex(s.getMaxU(), s.getMaxV()).lightmap(240, 0).endVertex();
            vb.pos(bb.maxX, bb.minY, bb.maxZ).color(153, 153, 153, 127).tex(s.getMaxU(), s.getMinV()).lightmap(240, 0).endVertex();
        }
        Minecraft.getMinecraft().mcProfiler.endSection();

        Minecraft.getMinecraft().mcProfiler.endSection();
    }
}
