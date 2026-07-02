/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.builders.addon;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.mojang.blaze3d.vertex.BufferBuilder;

import ct.buildcraft.core.marker.volume.IFastAddonRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class AddonRendererFillerPlanner implements IFastAddonRenderer<AddonFillerPlanner> {
    @Override
    public void renderAddonFast(AddonFillerPlanner addon, Player player, float partialTicks, BufferBuilder vb) {
        if (addon.buildingInfo == null) {
            return;
        }
//        Minecraft.getInstance().getProfiler().push("filler_planner");

//        Minecraft.getInstance().getProfiler().push("iter");
        List<BlockPos> list = StreamSupport.stream(
            BlockPos.betweenClosed(addon.buildingInfo.box.min(), addon.buildingInfo.box.max()).spliterator(),
            false
        )
            .filter(blockPos ->
                addon.buildingInfo.getSnapshot().data.get(
                    addon.buildingInfo.getSnapshot().posToIndex(
                        addon.buildingInfo.fromWorld(blockPos)
                    )
                )
            )
            .filter(player.level::isEmptyBlock)
            .collect(Collectors.toCollection(ArrayList::new));
//        Minecraft.getInstance().getProfiler().pop();

  //      Minecraft.getInstance().getProfiler().push("sort");
        list.sort(Comparator.<BlockPos>comparingDouble(p -> player.distanceToSqr(Vec3.atLowerCornerOf(p))).reversed());
  //      Minecraft.getInstance().getProfiler().pop();

    //    Minecraft.getInstance().getProfiler().push("render");
        for (BlockPos p : list) {
            AABB bb = new AABB(p, p.offset(1, 1, 1)).inflate(-0.1);
            TextureAtlasSprite s = Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS).getSprite(new ResourceLocation("quartz_block_top"));//ModelLoader.White.INSTANCE;

            vb.vertex(bb.minX, bb.maxY, bb.minZ).color(204, 204, 204, 127).uv(s.getU0(), s.getV0()).uv2(240, 0).endVertex();
            vb.vertex(bb.maxX, bb.maxY, bb.minZ).color(204, 204, 204, 127).uv(s.getU0(), s.getV1()).uv2(240, 0).endVertex();
            vb.vertex(bb.maxX, bb.minY, bb.minZ).color(204, 204, 204, 127).uv(s.getU1(), s.getV1()).uv2(240, 0).endVertex();
            vb.vertex(bb.minX, bb.minY, bb.minZ).color(204, 204, 204, 127).uv(s.getU1(), s.getV0()).uv2(240, 0).endVertex();

            vb.vertex(bb.minX, bb.minY, bb.maxZ).color(204, 204, 204, 127).uv(s.getU0(), s.getV0()).uv2(240, 0).endVertex();
            vb.vertex(bb.maxX, bb.minY, bb.maxZ).color(204, 204, 204, 127).uv(s.getU0(), s.getV1()).uv2(240, 0).endVertex();
            vb.vertex(bb.maxX, bb.maxY, bb.maxZ).color(204, 204, 204, 127).uv(s.getU1(), s.getV1()).uv2(240, 0).endVertex();
            vb.vertex(bb.minX, bb.maxY, bb.maxZ).color(204, 204, 204, 127).uv(s.getU1(), s.getV0()).uv2(240, 0).endVertex();

            vb.vertex(bb.minX, bb.minY, bb.minZ).color(127, 127, 127, 127).uv(s.getU0(), s.getV0()).uv2(240, 0).endVertex();
            vb.vertex(bb.maxX, bb.minY, bb.minZ).color(127, 127, 127, 127).uv(s.getU0(), s.getV1()).uv2(240, 0).endVertex();
            vb.vertex(bb.maxX, bb.minY, bb.maxZ).color(127, 127, 127, 127).uv(s.getU1(), s.getV1()).uv2(240, 0).endVertex();
            vb.vertex(bb.minX, bb.minY, bb.maxZ).color(127, 127, 127, 127).uv(s.getU1(), s.getV0()).uv2(240, 0).endVertex();

            vb.vertex(bb.minX, bb.maxY, bb.maxZ).color(255, 255, 255, 127).uv(s.getU0(), s.getV0()).uv2(240, 0).endVertex();
            vb.vertex(bb.maxX, bb.maxY, bb.maxZ).color(255, 255, 255, 127).uv(s.getU0(), s.getV1()).uv2(240, 0).endVertex();
            vb.vertex(bb.maxX, bb.maxY, bb.minZ).color(255, 255, 255, 127).uv(s.getU1(), s.getV1()).uv2(240, 0).endVertex();
            vb.vertex(bb.minX, bb.maxY, bb.minZ).color(255, 255, 255, 127).uv(s.getU1(), s.getV0()).uv2(240, 0).endVertex();

            vb.vertex(bb.minX, bb.minY, bb.maxZ).color(153, 153, 153, 127).uv(s.getU0(), s.getV0()).uv2(240, 0).endVertex();
            vb.vertex(bb.minX, bb.maxY, bb.maxZ).color(153, 153, 153, 127).uv(s.getU0(), s.getV1()).uv2(240, 0).endVertex();
            vb.vertex(bb.minX, bb.maxY, bb.minZ).color(153, 153, 153, 127).uv(s.getU1(), s.getV1()).uv2(240, 0).endVertex();
            vb.vertex(bb.minX, bb.minY, bb.minZ).color(153, 153, 153, 127).uv(s.getU1(), s.getV0()).uv2(240, 0).endVertex();

            vb.vertex(bb.maxX, bb.minY, bb.minZ).color(153, 153, 153, 127).uv(s.getU0(), s.getV0()).uv2(240, 0).endVertex();
            vb.vertex(bb.maxX, bb.maxY, bb.minZ).color(153, 153, 153, 127).uv(s.getU0(), s.getV1()).uv2(240, 0).endVertex();
            vb.vertex(bb.maxX, bb.maxY, bb.maxZ).color(153, 153, 153, 127).uv(s.getU1(), s.getV1()).uv2(240, 0).endVertex();
            vb.vertex(bb.maxX, bb.minY, bb.maxZ).color(153, 153, 153, 127).uv(s.getU1(), s.getV0()).uv2(240, 0).endVertex();
        }
//        Minecraft.getInstance().getProfiler().pop();

//        Minecraft.getInstance().getProfiler().pop();
    }
}
