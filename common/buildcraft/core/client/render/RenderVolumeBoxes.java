/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.client.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;

import buildcraft.lib.client.render.DetachedRenderer;
import buildcraft.lib.client.render.laser.LaserBoxRenderer;
import buildcraft.lib.client.render.laser.LaserData_BC8.LaserType;

import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.core.marker.volume.Addon;
import buildcraft.core.marker.volume.ClientVolumeBoxes;
import buildcraft.core.marker.volume.IFastAddonRenderer;
import buildcraft.core.marker.volume.Lock;

public enum RenderVolumeBoxes implements DetachedRenderer.IDetachedRenderer {
    INSTANCE;

    private static final double NORMAL_SCALE = 1 / 16D;
    private static final double HIGHLIGHT_SCALE = 1 / 15.8D;

    @Override
    public void render(EntityPlayer player, float partialTicks) {
        GlStateManager.enableBlend();

        BufferBuilder bb = Tessellator.getInstance().getBuffer();

        bb.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        ClientVolumeBoxes.INSTANCE.volumeBoxes.forEach(volumeBox -> {
            LaserType type;
            if (volumeBox.isEditingBy(player)) {
                type = BuildCraftLaserManager.MARKER_VOLUME_SIGNAL;
            } else {
                type =  volumeBox.getLockTargetsStream()
                        .filter(Lock.Target.TargetUsedByMachine.class::isInstance)
                        .map(Lock.Target.TargetUsedByMachine.class::cast)
                        .map(target -> target.type)
                        .map(Lock.Target.TargetUsedByMachine.EnumType::getLaserType)
                        .findFirst()
                        .orElse(BuildCraftLaserManager.MARKER_VOLUME_CONNECTED);
            }
            LaserBoxRenderer.renderLaserBoxDynamic(volumeBox.box, type, bb, false);

            volumeBox.addons.values().forEach(addon ->
                ((IFastAddonRenderer<Addon>) addon.getRenderer()).renderAddonFast(addon, player, partialTicks, bb)
            );
        });

        Tessellator.getInstance().draw();

        GlStateManager.disableBlend();
    }
}
