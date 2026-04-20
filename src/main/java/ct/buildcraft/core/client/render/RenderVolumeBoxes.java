/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.core.client.render;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;

import ct.buildcraft.core.client.BuildCraftLaserManager;
import ct.buildcraft.core.marker.volume.Addon;
import ct.buildcraft.core.marker.volume.ClientVolumeBoxes;
import ct.buildcraft.core.marker.volume.IFastAddonRenderer;
import ct.buildcraft.core.marker.volume.Lock;
import ct.buildcraft.lib.client.render.DetachedRenderer;
import ct.buildcraft.lib.client.render.laser.LaserBoxRenderer;
import ct.buildcraft.lib.client.render.laser.LaserData_BC8.LaserType;
import net.minecraft.world.entity.player.Player;

public enum RenderVolumeBoxes implements DetachedRenderer.IDetachedRenderer {
    INSTANCE;
	
    @Override
	public void render(PoseStack pose, Matrix4f matrix, Player player, float partialTicks) {
    	
    	BufferBuilder bb = Tesselator.getInstance().getBuilder();
    	bb.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
        ClientVolumeBoxes.INSTANCE.volumeBoxes.forEach(volumeBox -> {
            LaserType type;
            if (volumeBox.isEditingBy(player)) {
                type = BuildCraftLaserManager.MARKER_VOLUME_SIGNAL;
            } else {
                type = volumeBox.getLockTargetsStream()
                    .filter(Lock.Target.TargetUsedByMachine.class::isInstance)
                    .map(Lock.Target.TargetUsedByMachine.class::cast)
                    .map(target -> target.type)
                    .map(Lock.Target.TargetUsedByMachine.EnumType::getLaserType)
                    .findFirst()
                    .orElse(BuildCraftLaserManager.MARKER_VOLUME_CONNECTED);
            }
            LaserBoxRenderer.renderLaserBoxDynamic(volumeBox.box, type, pose.last().pose(), pose.last().normal(), bb, false);

            volumeBox.addons.values().forEach(addon ->
                ((IFastAddonRenderer<Addon>) addon.getRenderer()).renderAddonFast(addon, player, partialTicks, bb)
            );
        });
        Tesselator.getInstance().end();
		
	}

}
