/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.debug;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

import ct.buildcraft.lib.client.model.ModelUtil;
import ct.buildcraft.lib.client.model.MutableQuad;
import ct.buildcraft.lib.client.render.DetachedRenderer.IDetachedRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum DebugRenderHelper implements IDetachedRenderer {
    INSTANCE;

    private static final MutableQuad[] smallCuboid;
    
    private static TextureAtlasSprite s;

    static {
        smallCuboid = new MutableQuad[6];
        Vector3f center = new Vector3f(0.5f, 0.5f, 0.5f);
        Vector3f radius = new Vector3f(0.25f, 0.25f, 0.25f);

        for (Direction face : Direction.values()) {
            MutableQuad quad = ModelUtil.createFace(face, center, radius, null);
            quad.lightf(1, 1);
            smallCuboid[face.ordinal()] = quad;
        }
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(PoseStack pose, Matrix4f matrix, Player player, float partialTicks) {
        IAdvDebugTarget target = BCAdvDebugging.INSTANCE.targetClient;
        if (target == null) {
            return;
        } else if (!target.doesExistInWorld()) {
            // targetClient = null;
            // return;
        }
        IDetachedRenderer renderer = target.getDebugRenderer();
        if (renderer != null) {
            renderer.render(pose, matrix, player, partialTicks);
        }
    }

    public static void renderAABB(PoseStack pose, Matrix4f matrix, BufferBuilder bb, AABB aabb, int colour) {
    	if(s == null) s = Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS).getSprite(new ResourceLocation("quartz_block_top"));//TODO change to white
    	pose.pushPose();
    	pose.translate(0, 0, 0);
    	Matrix3f normal = pose.last().normal();
        for (Direction face : Direction.values()) {
            MutableQuad quad = ModelUtil.createFace(
                face,
                new Vector3f(
                    (float) aabb.getCenter().x,
                    (float) aabb.getCenter().y,
                    (float) aabb.getCenter().z
                ),
                new Vector3f(
                    (float) (aabb.maxX - aabb.minX) / 2,
                    (float) (aabb.maxY - aabb.minY) / 2,
                    (float) (aabb.maxZ - aabb.minZ) / 2
                ),
                null
            );
            quad.lightf(1, 1);
            quad.texFromSprite(s);
            quad.colouri(colour);
            quad.render(matrix, normal, bb);
        }
        pose.popPose();
    }

    public static void renderSmallCuboid(PoseStack pose, Matrix4f matrix, BufferBuilder bb, BlockPos pos, int colour) {
    	if(s == null) s = Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS).getSprite(new ResourceLocation("quartz_block_top"));//TODO change to white
    	pose.pushPose();
        pose.translate(pos.getX(), pos.getY(), pos.getZ());
        Matrix3f normal = pose.last().normal();
        for (MutableQuad q : smallCuboid) {
            q.texFromSprite(s);
            q.colouri(colour);
            q.render(matrix, normal, bb);
        }
        pose.translate(0,0,0);
        pose.popPose();
    }
}
