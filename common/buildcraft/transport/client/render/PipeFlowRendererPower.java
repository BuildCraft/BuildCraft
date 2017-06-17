/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.render;

import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Point3f;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.transport.pipe.IPipeFlowRenderer;

import buildcraft.lib.client.model.ModelUtil;

import buildcraft.transport.BCTransportSprites;
import buildcraft.transport.pipe.flow.PipeFlowPower;

@SideOnly(Side.CLIENT)
public enum PipeFlowRendererPower implements IPipeFlowRenderer<PipeFlowPower> {
    INSTANCE;

    @Override
    public void render(PipeFlowPower flow, double x, double y, double z, float partialTicks, BufferBuilder vb) {
        float r = (float) flow.clientPowerAmounts.values().stream()
            .mapToDouble(Double::valueOf)
            .average()
            .orElse(0) / PipeFlowPower.DEFAULT_MAX_POWER * 0.8F;
        if (r == 0) {
            return;
        }
        List<Triple<Pair<EnumFacing, EnumFacing>, Point3f, Point3f>> facesSidesCentersRadiuses = new ArrayList<>();
        for (EnumFacing face : EnumFacing.VALUES) {
            facesSidesCentersRadiuses.add(
                Triple.of(
                    Pair.of(face, null),
                    new Point3f(0.5F, 0.5F, 0.5F),
                    new Point3f(r, r, r)
                )
            );
            for (EnumFacing side : EnumFacing.VALUES) {
                if (!flow.pipe.isConnected(side)) {
                    continue;
                }
                facesSidesCentersRadiuses.add(
                    Triple.of(
                        Pair.of(face, side),
                        new Point3f(
                            0.5F + side.getFrontOffsetX() * (0.25F + r / 2),
                            0.5F + side.getFrontOffsetY() * (0.25F + r / 2),
                            0.5F + side.getFrontOffsetZ() * (0.25F + r / 2)
                        ),
                        new Point3f(
                            side.getAxis() == EnumFacing.Axis.X ? 0.25F - r / 2 : r,
                            side.getAxis() == EnumFacing.Axis.Y ? 0.25F - r / 2 : r,
                            side.getAxis() == EnumFacing.Axis.Z ? 0.25F - r / 2 : r
                        )
                    )
                );
            }
        }
        vb.setTranslation(x, y, z);
        for (Triple<Pair<EnumFacing, EnumFacing>, Point3f, Point3f> faceSideCenterRadius : facesSidesCentersRadiuses) {
            EnumFacing face = faceSideCenterRadius.getLeft().getLeft();
            EnumFacing side = faceSideCenterRadius.getLeft().getRight();
            Point3f center = faceSideCenterRadius.getMiddle();
            Point3f radius = faceSideCenterRadius.getRight();
            ModelUtil.UvFaceData uvs = null;
            switch (face.getAxis()) {
                case X:
                    uvs = new ModelUtil.UvFaceData(
                        center.getZ() - radius.getZ(),
                        center.getY() - radius.getY(),
                        center.getZ() + radius.getZ(),
                        center.getY() + radius.getY()
                    );
                    break;
                case Y:
                    uvs = new ModelUtil.UvFaceData(
                        center.getX() - radius.getX(),
                        center.getZ() - radius.getZ(),
                        center.getX() + radius.getX(),
                        center.getZ() + radius.getZ()
                    );
                    break;
                case Z:
                    uvs = new ModelUtil.UvFaceData(
                        center.getX() - radius.getX(),
                        center.getY() - radius.getY(),
                        center.getX() + radius.getX(),
                        center.getY() + radius.getY()
                    );
                    break;
            }
            boolean invert = false;
            if (side != null && face.getAxis() == EnumFacing.Axis.X && side.getAxis() == EnumFacing.Axis.Y) {
                invert = true;
            }
            if (side != null && face.getAxis() == EnumFacing.Axis.Y && side.getAxis() == EnumFacing.Axis.Z) {
                invert = true;
            }
            if (side != null && face.getAxis() == EnumFacing.Axis.Z && side.getAxis() == EnumFacing.Axis.Y) {
                invert = true;
            }
            if (invert) {
                uvs = new ModelUtil.UvFaceData(
                    1 - uvs.maxU,
                    1 - uvs.maxV,
                    1 - uvs.minU,
                    1 - uvs.minV
                );
            }
            uvs = new ModelUtil.UvFaceData(
                BCTransportSprites.POWER_FLOW.getInterpU(uvs.minU),
                BCTransportSprites.POWER_FLOW.getInterpV(uvs.minV),
                BCTransportSprites.POWER_FLOW.getInterpU(uvs.maxU),
                BCTransportSprites.POWER_FLOW.getInterpV(uvs.maxV)
            );
            ModelUtil.createFace(face, center, radius, uvs)
                .lighti(15, 15)
                .render(vb);
        }
        vb.setTranslation(0, 0, 0);
    }
}
