/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.builders;

import buildcraft.api.schematics.ISchematicBlock;
import buildcraft.builders.client.ClientArchitectTables;
import buildcraft.builders.item.ItemSchematicSingle;
import buildcraft.builders.snapshot.Blueprint;
import buildcraft.builders.snapshot.ClientSnapshots;
import buildcraft.builders.snapshot.Snapshot;
import buildcraft.builders.snapshot.Snapshot.Header;
import buildcraft.builders.tile.TileQuarry;
import buildcraft.lib.misc.RenderUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.gui.GuiUtils;

import java.lang.ref.WeakReference;
import java.util.*;

public enum BCBuildersEventDist {
    INSTANCE;

    private static final UUID UUID_SINGLE_SCHEMATIC = new UUID(0xfd3b8c59b0a8b191L, 0x772ec006c1b0ffaaL);
    private final Map<World, Deque<WeakReference<TileQuarry>>> allQuarries = new WeakHashMap<>();

    public synchronized void validateQuarry(TileQuarry quarry) {
        Deque<WeakReference<TileQuarry>> quarries =
                allQuarries.computeIfAbsent(quarry.getLevel(), k -> new LinkedList<>());
        quarries.add(new WeakReference<>(quarry));
    }

    public synchronized void invalidateQuarry(TileQuarry quarry) {
        Deque<WeakReference<TileQuarry>> quarries = allQuarries.get(quarry.getLevel());
        if (quarries == null) {
            // Odd.
            return;
        }
        Iterator<WeakReference<TileQuarry>> iter = quarries.iterator();
        while (iter.hasNext()) {
            WeakReference<TileQuarry> ref = iter.next();
            TileQuarry pos = ref.get();
            if (pos == null || pos == quarry) {
                iter.remove();
            }
        }
    }

    // TODO Calen how to add collision boxes to world??? PlayerEntity#maybeBackOffFromEdge:1044 this.level.noCollision(...) mixin??? create entities?
//    @SubscribeEvent
//    public synchronized void onGetCollisionBoxesForQuarry(GetCollisionBoxesEvent event)
//    {
//        Deque<WeakReference<TileQuarry>> quarries = allQuarries.get(event.getWorld());
//        if (quarries == null)
//        {
//            // No quarries in the target world
//            return;
//        }
//        Iterator<WeakReference<TileQuarry>> iter = quarries.iterator();
//        while (iter.hasNext())
//        {
//            WeakReference<TileQuarry> ref = iter.next();
//            TileQuarry quarry = ref.get();
//            if (quarry == null)
//            {
//                iter.remove();
//                continue;
//            }
//            for (AxisAlignedBB aabb : quarry.getCollisionBoxes())
//            {
//                if (event.getAabb().intersects(aabb))
//                {
//                    event.getCollisionBoxesList().add(aabb);
//                }
//            }
//        }
//    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
//    public void onRenderTooltipPostText(RenderTooltipEvent.PostText event)
    public void onRenderTooltipPostText(RenderTooltipEvent.Color event) {
        Snapshot snapshot = null;
//        ItemStack stack = event.getStack();
        ItemStack stack = event.getStack();
//        Header header = BCBuildersItems.snapshotBLUEPRINT_CLEAN != null ? BCBuildersItems.snapshotBLUEPRINT_CLEAN.get().getHeader(stack) : null;
        Header header = BCBuildersItems.snapshotBLUEPRINT != null ? BCBuildersItems.snapshotBLUEPRINT.get().getHeader(stack) : null;
        if (header != null) {
            snapshot = ClientSnapshots.INSTANCE.getSnapshot(header.key);
        } else if (BCBuildersItems.schematicSingle != null) {
            ISchematicBlock schematicBlock = ItemSchematicSingle.getSchematicSafe(stack);
            if (schematicBlock != null) {
                Blueprint blueprint = new Blueprint();
                blueprint.size = new BlockPos(1, 1, 1);
                blueprint.offset = BlockPos.ZERO;
                blueprint.data = new int[] { 0 };
                blueprint.palette.add(schematicBlock);
                blueprint.computeKey();
                snapshot = blueprint;
            }
        }

        if (snapshot != null) {
            int pX = event.getX();
//            int pY = event.getY() + event.getHeight() + 10;
            int pY = event.getY() + 10;
            int lineHeight = event.getFontRenderer().lineHeight;
            for (ITextProperties line : event.getLines()) {
//                pY += line.getHeight();
                pY += lineHeight;
            }
            pY += 3; // Calen: GuiUtils#drawHoveringText:tooltipHeight + 3
            int sX = 100;
            int sY = 100;

            MatrixStack poseStack = event.getMatrixStack();
            Matrix4f pose = poseStack.last().pose();

            // Copy from GuiUtils#drawHoveringText
            // Calen: z = 400 in 1.18.2
//            int zLevel = 300;
            int zLevel = 400;
            int backgroundColor = 0xF0100010;
            // 1.16.5: should use GuiUtils.drawGradientRect because bufferbuilder not started
            // 1.18.2: should not use GuiUtils.drawGradientRect because duplicated bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR)
            // Calen: z is after x y in 1.18.2
            GuiUtils.drawGradientRect(pose, zLevel, pX - 3, pY - 4, pX + sX + 3, pY - 3, backgroundColor, backgroundColor);
            GuiUtils.drawGradientRect(pose, zLevel, pX - 3, pY + sY + 3, pX + sX + 3, pY + sY + 4, backgroundColor, backgroundColor);
            GuiUtils.drawGradientRect(pose, zLevel, pX - 3, pY - 3, pX + sX + 3, pY + sY + 3, backgroundColor, backgroundColor);
            GuiUtils.drawGradientRect(pose, zLevel, pX - 4, pY - 3, pX - 3, pY + sY + 3, backgroundColor, backgroundColor);
            GuiUtils.drawGradientRect(pose, zLevel, pX + sX + 3, pY - 3, pX + sX + 4, pY + sY + 3, backgroundColor, backgroundColor);
            int borderColorStart = 0x505000FF;
            int borderColorEnd = (borderColorStart & 0xFEFEFE) >> 1 | borderColorStart & 0xFF000000;
            GuiUtils.drawGradientRect(pose, zLevel, pX - 3, pY - 3 + 1, pX - 3 + 1, pY + sY + 3 - 1, borderColorStart, borderColorEnd);
            GuiUtils.drawGradientRect(pose, zLevel, pX + sX + 2, pY - 3 + 1, pX + sX + 3, pY + sY + 3 - 1, borderColorStart, borderColorEnd);
            GuiUtils.drawGradientRect(pose, zLevel, pX - 3, pY - 3, pX + sX + 3, pY - 3 + 1, borderColorStart, borderColorStart);
            GuiUtils.drawGradientRect(pose, zLevel, pX - 3, pY + sY + 2, pX + sX + 3, pY + sY + 3, borderColorEnd, borderColorEnd);

            // Calen: draw and recover the context
            RenderSystem.enableDepthTest();
            RenderSystem.disableTexture();
            RenderUtil.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.enableTexture();

            poseStack.pushPose();
            ClientSnapshots.INSTANCE.renderSnapshot(snapshot, pX, pY, sX, sY, poseStack);
            poseStack.popPose();
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onTickClientTick(TickEvent.ClientTickEvent event) {
//        if (event.phase == TickEvent.Phase.END && !Minecraft.getMinecraft().isGamePaused())
        if (event.phase == TickEvent.Phase.END && !Minecraft.getInstance().isPaused()) {
            ClientArchitectTables.tick();
        }
    }
}
