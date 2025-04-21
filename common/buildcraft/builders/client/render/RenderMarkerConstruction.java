/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.client.render;

import buildcraft.builders.tile.TileMarkerConstruction;
import buildcraft.core.BCCoreConfig;
import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.lib.client.render.ItemRenderUtil;
import buildcraft.lib.client.render.laser.LaserBoxRenderer;
import buildcraft.lib.client.render.laser.LaserData_BC8;
import buildcraft.lib.client.render.laser.LaserRenderer_BC8;
import buildcraft.lib.misc.RenderUtil;
import buildcraft.lib.misc.data.Box;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public class RenderMarkerConstruction implements BlockEntityRenderer<TileMarkerConstruction> {
    // private final RenderBuildingItems renderItems = new RenderBuildingItems();

    public RenderMarkerConstruction(BlockEntityRendererProvider.Context context) {
    }

    @Override
    // public void renderTileEntityAt(TileConstructionMarker marker, double x, double y, double z, float f, int aThing)
    public void render(TileMarkerConstruction marker, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        // super.renderTileEntityAt(marker, x, y, z, f, aThing);
        renderBox(marker, poseStack, bufferSource);

        if (marker != null) {
            // GL11.glPushMatrix();
            poseStack.pushPose();
            // GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
            // GlStateManager.enableCull();
            // GlStateManager.enableLighting();
            // GlStateManager.enableAlpha();
            // GlStateManager.enableBlend();
            // GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            // GL11.glTranslated(x, y, z);
            // GL11.glTranslated(-marker.getPos().getX(), -marker.getPos().getY(), -marker.getPos().getZ());
            poseStack.translate(-marker.getBlockPos().getX(), -marker.getBlockPos().getY(), -marker.getBlockPos().getZ());

            if (marker.laser != null) {
                // GL11.glPushMatrix();
                poseStack.pushPose();
                // RenderLaser.doRenderLaser(TileEntityRendererDispatcher.instance.worldObj, Minecraft.getMinecraft().renderEngine, marker.laser, EntityLaser.LASER_STRIPES_YELLOW);
                VertexConsumer buffer = bufferSource.getBuffer(RenderType.translucent());
                // LaserRenderer_BC8.renderLaserDynamic(marker.laser, poseStack.last(), buffer);
                LaserData_BC8 laser = new LaserData_BC8(BuildCraftLaserManager.STRIPES_WRITE, marker.laser.getFirst(), marker.laser.getSecond(), 0.5 / 16.0);
                LaserRenderer_BC8.renderLaserStatic(laser, poseStack.last());
                // GL11.glPopMatrix();
                poseStack.popPose();
            }

            // if (marker.itemBlueprint != null)
            if (!marker.itemBlueprint.isEmpty()) {
                VertexConsumer buffer = bufferSource.getBuffer(Sheets.translucentCullBlockSheet());
                doRenderItem(
                        poseStack,
                        buffer,
                        marker.itemBlueprint,
                        marker.getBlockPos().getX() + 0.5F,
                        marker.getBlockPos().getY() + 0.2F,
                        marker.getBlockPos().getZ() + 0.5F,
                        RenderUtil.getCombinedLight(marker.getLevel(), new BlockPos(marker.getBlockPos())),
                        marker.direction
                );
            }

            // GlStateManager.disableBlend();
            // GL11.glPopAttrib();
            // GL11.glPopMatrix();
            poseStack.popPose();

            // renderItems.render(marker, x, y, z);
            if (marker.bluePrintBuilder != null) {
                VertexConsumer buffer = bufferSource.getBuffer(Sheets.translucentCullBlockSheet());
                RenderSnapshotBuilder.render(marker.bluePrintBuilder, marker.getLevel(), marker.getBlockPos(), partialTicks, poseStack, buffer);
            }
        }
    }

    // public void doRenderItem(ItemStack stack, double x, double y, double z)
    public void doRenderItem(PoseStack poseStack, VertexConsumer buffer, ItemStack stack, double x, double y, double z, int combinedLight, Direction facing) {
        if (stack == null) {
            return;
        }

        float renderScale = 1.5f;
//        GL11.glPushMatrix();
        poseStack.pushPose();
//        GL11.glTranslatef((float) x, (float) y, (float) z);
        poseStack.translate((float) x, (float) y, (float) z);
//        GL11.glTranslatef(0, 0.25F, 0);
        poseStack.translate(0, 0.25F, 0);
//        GL11.glScalef(renderScale, renderScale, renderScale);
        poseStack.scale(renderScale, renderScale, renderScale);
//        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
//        IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(stack);
//        Minecraft.getMinecraft().getRenderItem().renderItem(stack, model);
        ItemRenderUtil.renderItemStack(stack, combinedLight, facing, poseStack, buffer);

//        GL11.glPopMatrix();
        poseStack.popPose();
    }

    // @Override
    // public void renderTileEntityAt(TileConstructionMarker marker, double x, double y, double z, float f, int aThing)
    public void renderBox(TileMarkerConstruction tileentity, PoseStack poseStack, MultiBufferSource bufferSource) {
        // GL11.glPushMatrix();
        // GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        // GlStateManager.enableCull();
        // GlStateManager.disableLighting();
        // GlStateManager.enableBlend();
        // GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        VertexConsumer buffer = bufferSource.getBuffer(Sheets.translucentCullBlockSheet());

        // GL11.glPushMatrix();
        poseStack.pushPose();
        // GL11.glTranslated(-tileentity.getPos().getX(), -tileentity.getPos().getY(), -tileentity.getPos().getZ());
        // GL11.glTranslated(x, y, z);
        poseStack.translate(-tileentity.getBlockPos().getX(), -tileentity.getBlockPos().getY(), -tileentity.getBlockPos().getZ());

//        if (tileentity instanceof IBoxesProvider) {
//            for (Box b : ((IBoxesProvider) tileentity).getBoxes()) {
//                if (b.isVisible) {
//                    RenderBox.doRender(TileEntityRendererDispatcher.instance.worldObj, Minecraft.getMinecraft().renderEngine, getTexture(b.kind), b);
//                }
//            }
//        } else if (tileentity instanceof IBoxProvider) {
//            Box b = ((IBoxProvider) tileentity).getBox();
//
//            if (b.isVisible && b.isInitialized()) {
//                RenderBox.doRender(TileEntityRendererDispatcher.instance.worldObj, Minecraft.getMinecraft().renderEngine, getTexture(b.kind), b);
//            }
//        }
        Box box = tileentity.box;
        // LaserBoxRenderer.renderLaserBoxDynamic(box, BuildCraftLaserManager.STRIPES_WRITE, poseStack.last(), buffer, true);
        LaserBoxRenderer.renderLaserBoxStatic(box, BuildCraftLaserManager.STRIPES_WRITE, poseStack.last(), true);

        // GL11.glPopMatrix();
        poseStack.popPose();
        // GL11.glPopAttrib();
        // GL11.glPopMatrix();
    }

    @Override
    public boolean shouldRenderOffScreen(TileMarkerConstruction te) {
        return true;
    }

    @Override
    public int getViewDistance() {
        // Calen: as beacon and endGateway
        return BCCoreConfig.markerMaxDistance * 2;
    }
}
