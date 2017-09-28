/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.addon;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import net.minecraftforge.client.model.ModelLoader;

import buildcraft.api.filler.FilledTemplate.TemplateState;

import buildcraft.builders.snapshot.Template;
import buildcraft.core.marker.volume.IFastAddonRenderer;

public class AddonRendererFillingPlanner implements IFastAddonRenderer<AddonFillingPlanner> {
    @Override
    public void renderAddonFast(AddonFillingPlanner addon, EntityPlayer player, float partialTicks, VertexBuffer vb) {
        Template.BuildingInfo buildingInfo = addon.buildingInfo;
        if (buildingInfo == null) {
            return;
        }
        Profiler prof = Minecraft.getMinecraft().mcProfiler;
        prof.startSection("filler_planner");
        prof.startSection("iter");
        List<BlockPos> list = new ArrayList<>();
        for (BlockPos p : BlockPos.getAllInBoxMutable(buildingInfo.box.min(), buildingInfo.box.max())) {
            if (buildingInfo.getSnapshot().data.getOffset(buildingInfo.fromWorld(p)) == TemplateState.FILL && player.world.isAirBlock(p)) {
                list.add(p.toImmutable());
            }
        }

        prof.endStartSection("sort");
        list.sort(Comparator.<BlockPos>comparingDouble(p -> player.getPositionVector().squareDistanceTo(new Vec3d(p))).reversed());

        prof.endStartSection("render");
        for (BlockPos p : list) {
            AxisAlignedBB bb = new AxisAlignedBB(p, p.add(1, 1, 1)).expandXyz(-0.1);
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
        prof.endSection();
        prof.endSection();
    }
}
