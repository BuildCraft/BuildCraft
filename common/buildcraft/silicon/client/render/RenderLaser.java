/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.client.render;

import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.core.item.ItemGoggles;
import buildcraft.lib.client.render.laser.LaserData_BC8;
import buildcraft.lib.client.render.laser.LaserRenderer_BC8;
import buildcraft.silicon.BCSiliconConfig;
import buildcraft.silicon.tile.TileLaser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderLaser implements BlockEntityRenderer<TileLaser> {
    private static final int MAX_POWER = BuildCraftLaserManager.POWERS.length - 1;

    public RenderLaser(BlockEntityRendererProvider.Context context) {
    }

    @Override
//    public void renderTileEntityFast(@Nonnull TileLaser tile, double x, double y, double z, float partialTicks, int destroyStage, float partial, @Nonnull BufferBuilder buffer)
    public void render(TileLaser tile, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        if (BCSiliconConfig.renderLaserBeams || isPlayerWearingGoggles()) {
            Minecraft.getInstance().getProfiler().push("bc");
            Minecraft.getInstance().getProfiler().push("laser");

//            buffer.setTranslation(x - tile.getBlockPos().getX(), y - tile.getBlockPos().getY(), z - tile.getBlockPos().getZ());
            poseStack.pushPose();
            poseStack.translate(-tile.getBlockPos().getX(), -tile.getBlockPos().getY(), -tile.getBlockPos().getZ());

            if (tile.laserPos != null) {
                long avg = tile.getAverageClient();
                if (avg > 200_000) {
                    avg += 200_000;
                    Direction side = tile.getLevel().getBlockState(tile.getBlockPos()).getValue(BuildCraftProperties.BLOCK_FACING_6);
                    Vec3 offset = new Vec3(0.5, 0.5, 0.5).add(Vec3.atLowerCornerOf(side.getNormal()).scale(4 / 16D));
                    int index = (int) (avg * MAX_POWER / tile.getMaxPowerPerTick());
                    if (index > MAX_POWER) {
                        index = MAX_POWER;
                    }
                    LaserData_BC8 laser = new LaserData_BC8(BuildCraftLaserManager.POWERS[index], Vec3.atLowerCornerOf(tile.getBlockPos()).add(offset), tile.laserPos, 1 / 16D);
                    VertexConsumer buffer = bufferSource.getBuffer(ItemBlockRenderTypes.getRenderType(tile.getLevel().getBlockState(tile.getBlockPos()), true));
                    LaserRenderer_BC8.renderLaserDynamic(laser, poseStack.last(), buffer);
                }
            }

//            buffer.setTranslation(0, 0, 0);
            poseStack.popPose();


            Minecraft.getInstance().getProfiler().pop();
            Minecraft.getInstance().getProfiler().pop();
        }
    }

    private boolean isPlayerWearingGoggles() {
        Item headArmor = Minecraft.getInstance().player.getItemBySlot(EquipmentSlot.HEAD).getItem();
        return headArmor instanceof ItemGoggles;
    }
}
