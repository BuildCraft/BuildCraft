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
import buildcraft.silicon.BCSiliconBlocks;
import buildcraft.silicon.BCSiliconConfig;
import buildcraft.silicon.tile.TileLaser;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderLaser extends TileEntityRenderer<TileLaser> {
    private static final int MAX_POWER = BuildCraftLaserManager.POWERS.length - 1;

    public RenderLaser(TileEntityRendererDispatcher context) {
        super(context);
    }

    @Override
//    public void renderTileEntityFast(@Nonnull TileLaser tile, double x, double y, double z, float partialTicks, int destroyStage, float partial, @Nonnull BufferBuilder buffer)
    public void render(TileLaser tile, float partialTicks, MatrixStack poseStack, IRenderTypeBuffer bufferSource, int combinedLight, int combinedOverlay) {
        BlockState state = tile.getLevel().getBlockState(tile.getBlockPos());
        if (state.getBlock() != BCSiliconBlocks.laser.get()) {
            return;
        }
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
                    Direction side = state.getValue(BuildCraftProperties.BLOCK_FACING_6);
                    Vector3d offset = new Vector3d(0.5, 0.5, 0.5).add(Vector3d.atLowerCornerOf(side.getNormal()).scale(4 / 16D));
                    int index = (int) (avg * MAX_POWER / tile.getMaxPowerPerTick());
                    if (index > MAX_POWER) {
                        index = MAX_POWER;
                    }
                    LaserData_BC8 laser = new LaserData_BC8(BuildCraftLaserManager.POWERS[index], Vector3d.atLowerCornerOf(tile.getBlockPos()).add(offset), tile.laserPos, 1 / 16D);
                    IVertexBuilder buffer = bufferSource.getBuffer(RenderTypeLookup.getRenderType(tile.getLevel().getBlockState(tile.getBlockPos()), true));
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
        Item headArmor = Minecraft.getInstance().player.getItemBySlot(EquipmentSlotType.HEAD).getItem();
        return headArmor instanceof ItemGoggles;
    }
}
