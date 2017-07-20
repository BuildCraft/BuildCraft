/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.debug;

import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.render.DetachedRenderer.IDetachedRenderer;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;

public enum DebugRenderHelper implements IDetachedRenderer {
    INSTANCE;

    private static final MutableQuad[] smallCuboid;

    static {
        smallCuboid = new MutableQuad[6];
        Tuple3f center = new Point3f(0.5f, 0.5f, 0.5f);
        Tuple3f radius = new Point3f(0.25f, 0.25f, 0.25f);

        for (EnumFacing face : EnumFacing.VALUES) {
            MutableQuad quad = ModelUtil.createFace(face, center, radius, null);
            quad.lightf(1, 1);
            smallCuboid[face.ordinal()] = quad;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void render(EntityPlayer player, float partialTicks) {
        IAdvDebugTarget target = BCAdvDebugging.INSTANCE.targetClient;
        if (target == null) {
            return;
        } else if (!target.doesExistInWorld()) {
            // targetClient = null;
            // return;
        }
        IDetachedRenderer renderer = target.getDebugRenderer();
        if (renderer != null) {
            renderer.render(player, partialTicks);
        }
    }

    public static void renderSmallCuboid(VertexBuffer vb, BlockPos pos, int colour) {
        vb.setTranslation(pos.getX(), pos.getY(), pos.getZ());
        for (MutableQuad q : smallCuboid) {
            q.texFromSprite(ModelLoader.White.INSTANCE);
            q.colouri(colour);
            q.render(vb);
        }
        vb.setTranslation(0, 0, 0);
    }
}
