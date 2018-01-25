/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.render;

import java.util.List;

import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import buildcraft.lib.item.ItemStackHelper;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.transport.pipe.IPipeFlowRenderer;

import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.ModelUtil.UvFaceData;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.render.ItemRenderUtil;
import buildcraft.lib.misc.ColourUtil;

import buildcraft.transport.BCTransportSprites;
import buildcraft.transport.pipe.flow.PipeFlowItems;
import buildcraft.transport.pipe.flow.TravellingItem;

@SideOnly(Side.CLIENT)
public enum PipeFlowRendererItems implements IPipeFlowRenderer<PipeFlowItems> {
    INSTANCE;

    private static final MutableQuad[] COLOURED_QUADS = new MutableQuad[6];

    public static void onModelBake() {
        Tuple3f center = new Point3f();
        Tuple3f radius = new Vector3f(0.2f, 0.2f, 0.2f);

        ISprite sprite = BCTransportSprites.COLOUR_ITEM_BOX;
        UvFaceData uvs = new UvFaceData();
        uvs.minU = (float) sprite.getInterpU(0);
        uvs.maxU = (float) sprite.getInterpU(1);
        uvs.minV = (float) sprite.getInterpV(0);
        uvs.maxV = (float) sprite.getInterpV(1);

        for (EnumFacing face : EnumFacing.VALUES) {
            MutableQuad q = ModelUtil.createFace(face, center, radius, uvs);
            q.setCalculatedDiffuse();
            COLOURED_QUADS[face.ordinal()] = q;
        }
    }

    @Override
    public void render(PipeFlowItems flow, double x, double y, double z, float partialTicks, VertexBuffer bb) {
        World world = flow.pipe.getHolder().getPipeWorld();
        long now = world.getTotalWorldTime();
        int lightc = world.getCombinedLight(flow.pipe.getHolder().getPipePos(), 0);

        List<TravellingItem> toRender = flow.getAllItemsForRender();

        for (TravellingItem item : toRender) {
            Vec3d pos = item.getRenderPosition(BlockPos.ORIGIN, now, partialTicks);

            ItemStack stack = item.clientItemLink.get();
            if (stack != null && !ItemStackHelper.isEmpty(stack)) {
                if (item.stackSize != stack.stackSize) {
                    stack = stack.copy();
                    stack.stackSize = item.stackSize;
                }
                ItemRenderUtil.renderItemStack(x + pos.xCoord, y + pos.yCoord, z + pos.zCoord,//
                        stack, lightc, item.getRenderDirection(now, partialTicks), bb);
            }
            if (item.colour != null) {
                bb.setTranslation(x + pos.xCoord, y + pos.yCoord, z + pos.zCoord);
                int col = ColourUtil.getLightHex(item.colour);
                int r = (col >> 16) & 0xFF;
                int g = (col >> 8) & 0xFF;
                int b = col & 0xFF;
                for (MutableQuad q : COLOURED_QUADS) {
                    MutableQuad q2 = new MutableQuad(q);
                    q2.lighti(lightc);
                    q2.multColouri(r, g, b, 255);
                    q2.render(bb);
                }
                bb.setTranslation(0, 0, 0);
            }
        }

        ItemRenderUtil.endItemBatch();
    }
}
