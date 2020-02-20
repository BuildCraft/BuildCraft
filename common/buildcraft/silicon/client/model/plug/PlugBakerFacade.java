/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.client.model.plug;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.Vec3d;

import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.BCModules;
import buildcraft.api.transport.pluggable.IPluggableStaticBaker;

import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.model.MutableVertex;
import buildcraft.lib.misc.VecUtil;

import buildcraft.silicon.client.model.key.KeyPlugFacade;
import buildcraft.silicon.plug.PluggableFacade;
import buildcraft.transport.BCTransportModels;
import buildcraft.transport.client.model.key.KeyPlugBlocker;

@SideOnly(Side.CLIENT)
public enum PlugBakerFacade implements IPluggableStaticBaker<KeyPlugFacade> {
    INSTANCE;

    private int getVertexIndex(List<Vec3d> positions,
                               EnumFacing.Axis axis,
                               boolean minOrMax1, boolean minOrMax2) {
        EnumFacing.Axis axis1, axis2;
        switch (axis) {
            case X:
                axis1 = EnumFacing.Axis.Y;
                axis2 = EnumFacing.Axis.Z;
                break;
            case Y:
                axis1 = EnumFacing.Axis.X;
                axis2 = EnumFacing.Axis.Z;
                break;
            case Z:
                axis1 = EnumFacing.Axis.X;
                axis2 = EnumFacing.Axis.Y;
                break;
            default:
                throw new IllegalArgumentException();
        }
        double min1 = positions.stream().mapToDouble(pos -> VecUtil.getValue(pos, axis1)).min().orElse(0);
        double min2 = positions.stream().mapToDouble(pos -> VecUtil.getValue(pos, axis2)).min().orElse(0);
        double max1 = positions.stream().mapToDouble(pos -> VecUtil.getValue(pos, axis1)).max().orElse(0);
        double max2 = positions.stream().mapToDouble(pos -> VecUtil.getValue(pos, axis2)).max().orElse(0);
        double center1 = (min1 + max1) / 2;
        double center2 = (min2 + max2) / 2;
        return positions.indexOf(
            positions.stream()
                .filter(pos ->
                    (minOrMax1 ? VecUtil.getValue(pos, axis1) < center1 : VecUtil.getValue(pos, axis1) > center1) &&
                        (minOrMax2 ? VecUtil.getValue(pos, axis2) < center2 : VecUtil.getValue(pos, axis2) > center2)
                )
                .findFirst()
                .orElse(positions.get(0))
        );
    }

    @SuppressWarnings("SuspiciousNameCombination")
    private List<MutableQuad> getTransformedQuads(IBlockState state,
                                                  IBakedModel model,
                                                  EnumFacing side,
                                                  Vec3d pos0, Vec3d pos1, Vec3d pos2, Vec3d pos3) {
        return model.getQuads(state, side, 0).stream()
            .map(quad -> {
                MutableQuad mutableQuad = new MutableQuad().fromBakedItem(quad);
                boolean positive = side.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE;
                Function<Vec3d, Vec3d> transformPosition = pos -> {
                    switch (side.getAxis()) {
                        case X:
                            return new Vec3d(
                                positive ? 1 - pos.z : pos.z,
                                pos.y,
                                pos.x
                            );
                        case Y:
                            return new Vec3d(
                                pos.x,
                                positive ? 1 - pos.z : pos.z,
                                pos.y
                            );
                        case Z:
                            return new Vec3d(
                                pos.y,
                                pos.x,
                                positive ? 1 - pos.z : pos.z
                            );
                        default:
                            throw new IllegalArgumentException();
                    }
                };
                List<Vec3d> poses = Arrays.asList(
                    transformPosition.apply(pos0),
                    transformPosition.apply(pos1),
                    transformPosition.apply(pos2),
                    transformPosition.apply(pos3)
                );
                List<MutableVertex> vertexes = Arrays.asList(
                    mutableQuad.vertex_0,
                    mutableQuad.vertex_1,
                    mutableQuad.vertex_2,
                    mutableQuad.vertex_3
                );
                List<Vec3d> vertexesPoses = vertexes.stream()
                    .map(vertex -> new Vec3d(vertex.position_x, vertex.position_y, vertex.position_z))
                    .collect(Collectors.toList());
                double minU = vertexes.stream().mapToDouble(vertex -> vertex.tex_u).min().orElse(0);
                double minV = vertexes.stream().mapToDouble(vertex -> vertex.tex_v).min().orElse(0);
                double maxU = vertexes.stream().mapToDouble(vertex -> vertex.tex_u).max().orElse(0);
                double maxV = vertexes.stream().mapToDouble(vertex -> vertex.tex_v).max().orElse(0);
                Stream.of(
                    Pair.of(false, false),
                    Pair.of(false, true),
                    Pair.of(true, true),
                    Pair.of(true, false)
                ).forEach(minOrMaxPair -> {
                    Vec3d newPos = poses.get(
                        getVertexIndex(poses, side.getAxis(), minOrMaxPair.getLeft(), minOrMaxPair.getRight())
                    );
                    MutableVertex vertex = vertexes.get(
                        getVertexIndex(vertexesPoses, side.getAxis(), minOrMaxPair.getLeft(), minOrMaxPair.getRight())
                    );
                    vertex.positiond(newPos.x, newPos.y, newPos.z);
                    switch (side.getAxis()) {
                        case X:
                            vertex.texf(
                                (float) (minU + (maxU - minU) * (positive ? (1 - newPos.z) : newPos.z)),
                                (float) (minV + (maxV - minV) * (1 - newPos.y))
                            );
                            break;
                        case Y:
                            vertex.texf(
                                (float) (minU + (maxU - minU) * newPos.x),
                                (float) (minV + (maxV - minV) * (positive ? newPos.z : (1 - newPos.z)))
                            );
                            break;
                        case Z:
                            vertex.texf(
                                (float) (minU + (maxU - minU) * (positive ? newPos.x : (1 - newPos.x))),
                                (float) (minV + (maxV - minV) * (1 - newPos.y))
                            );
                            break;
                    }
                });
                return mutableQuad;
            })
            .collect(Collectors.toList());
    }

    @SuppressWarnings("SuspiciousNameCombination")
    private Vec3d rotate(Vec3d vec, Rotation rotation) {
        switch (rotation) {
            case NONE:
                return new Vec3d(vec.x, vec.y, vec.z);
            case CLOCKWISE_90:
                return new Vec3d(1 - vec.y, 1 - vec.x, vec.z);
            case CLOCKWISE_180:
                return new Vec3d(1 - vec.x, 1 - vec.y, vec.z);
            case COUNTERCLOCKWISE_90:
                return new Vec3d(vec.y, vec.x, vec.z);
        }
        throw new IllegalArgumentException();
    }

    private void addRotatedQuads(List<MutableQuad> quads,
                                 IBlockState state,
                                 IBakedModel model,
                                 EnumFacing side,
                                 Rotation rotation,
                                 Vec3d pos0, Vec3d pos1, Vec3d pos2, Vec3d pos3) {
        quads.addAll(getTransformedQuads(
            state, model, side,
            rotate(pos0, rotation),
            rotate(pos1, rotation),
            rotate(pos2, rotation),
            rotate(pos3, rotation)
        ));
    }

    public List<MutableQuad> bakeForKey(KeyPlugFacade key) {
        IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(key.state);
        BlockRenderLayer renderLayer = MinecraftForgeClient.getRenderLayer();
        ForgeHooksClient.setRenderLayer(null);
        List<MutableQuad> quads = new ArrayList<>();
        int pS = PluggableFacade.SIZE;
        int nS = 16 - pS;
        if (!key.isHollow) {
            quads.addAll(getTransformedQuads(
                key.state, model, key.side,
                new Vec3d(0 / 16D, 16 / 16D, 0 / 16D),
                new Vec3d(16 / 16D, 16 / 16D, 0 / 16D),
                new Vec3d(16 / 16D, 0 / 16D, 0 / 16D),
                new Vec3d(0 / 16D, 0 / 16D, 0 / 16D)
            ));
            quads.addAll(getTransformedQuads(
                key.state, model, key.side.getOpposite(),
                new Vec3d(pS / 16D, nS / 16D, nS / 16D),
                new Vec3d(nS / 16D, nS / 16D, nS / 16D),
                new Vec3d(nS / 16D, pS / 16D, nS / 16D),
                new Vec3d(pS / 16D, pS / 16D, nS / 16D)
            ));
        }
        for (Rotation rotation : Rotation.values()) {
            if (key.isHollow) {
                addRotatedQuads(
                        quads, key.state, model, key.side, rotation,
                        new Vec3d(0 / 16D, rotation.ordinal() % 2 == 0 ? 4 / 16D : 0 / 16D, 0 / 16D),
                        new Vec3d(4 / 16D, rotation.ordinal() % 2 == 0 ? 4 / 16D : 0 / 16D, 0 / 16D),
                        new Vec3d(4 / 16D, rotation.ordinal() % 2 == 0 ? 16 / 16D : 12 / 16D, 0 / 16D),
                        new Vec3d(0 / 16D, rotation.ordinal() % 2 == 0 ? 16 / 16D : 12 / 16D, 0 / 16D)
                );
            }
            addRotatedQuads(
                quads, key.state, model, key.side.getOpposite(), rotation,
                new Vec3d(0 / 16D, 16 / 16D, 16 / 16D),
                new Vec3d(pS / 16D, nS / 16D, nS / 16D),
                new Vec3d(pS / 16D, pS / 16D, nS / 16D),
                new Vec3d(0 / 16D, 0 / 16D, 16 / 16D)
            );
            if (key.isHollow) {
                addRotatedQuads(
                    quads, key.state, model, key.side.getOpposite(), rotation,
                    new Vec3d(pS / 16D, rotation.ordinal() % 2 == 0 ? nS / 16D : 12 / 16D, nS / 16D),
                    new Vec3d(4 / 16D, rotation.ordinal() % 2 == 0 ? nS / 16D : 12 / 16D, nS / 16D),
                    new Vec3d(4 / 16D, rotation.ordinal() % 2 == 0 ? 4 / 16D : pS / 16D, nS / 16D),
                    new Vec3d(pS / 16D, rotation.ordinal() % 2 == 0 ? 4 / 16D : pS / 16D, nS / 16D)
                );
            }
        }
        if (key.isHollow) {
            for (EnumFacing facing : EnumFacing.VALUES) {
                if (facing.getAxis() != key.side.getAxis()) {
                    boolean positive = key.side.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE;
                    if (key.side.getAxis() == EnumFacing.Axis.Z && facing.getAxis() == EnumFacing.Axis.X ||
                        key.side.getAxis() == EnumFacing.Axis.X && facing.getAxis() == EnumFacing.Axis.Y ||
                        key.side.getAxis() == EnumFacing.Axis.Y && facing.getAxis() == EnumFacing.Axis.Z) {
                        quads.addAll(getTransformedQuads(
                            key.state, model, facing,
                            new Vec3d(positive ? 16 / 16D : pS / 16D, 4 / 16D, 12.003 / 16D),
                            new Vec3d(positive ? 16 / 16D : pS / 16D, 12 / 16D, 12.003 / 16D),
                            new Vec3d(positive ? nS / 16D : 0 / 16D, 12 / 16D, 12.003 / 16D),
                            new Vec3d(positive ? nS / 16D : 0 / 16D, 4 / 16D, 12.003 / 16D)
                        ));
                    } else {
                        quads.addAll(getTransformedQuads(
                            key.state, model, facing,
                            new Vec3d(4 / 16D, positive ? 16 / 16D : pS / 16D, 12.003 / 16D),
                            new Vec3d(4 / 16D, positive ? nS / 16D : 0 / 16D, 12.003 / 16D),
                            new Vec3d(12 / 16D, positive ? nS / 16D : 0 / 16D, 12.003 / 16D),
                            new Vec3d(12 / 16D, positive ? 16 / 16D : pS / 16D, 12.003 / 16D)
                        ));
                    }
                }
            }
        }
        ForgeHooksClient.setRenderLayer(renderLayer);
        for (MutableQuad quad : quads) {
            int tint = quad.getTint();
            if (tint != -1) {
                quad.setTint(tint * EnumFacing.VALUES.length + key.side.ordinal());
            }
        }
        return quads;
    }

    @Override
    public List<BakedQuad> bake(KeyPlugFacade key) {
        List<MutableQuad> mutableQuads = bakeForKey(key);
        List<BakedQuad> baked = new ArrayList<>();
        for (MutableQuad quad : mutableQuads) {
            baked.add(quad.toBakedItem());
        }
        if (BCModules.TRANSPORT.isLoaded() && key.state.isFullBlock() && !key.isHollow) {
            baked.addAll(TransportCompat.bakeBlocker(key.side));
        }
        return baked;
    }

    static final class TransportCompat {
        static List<BakedQuad> bakeBlocker(EnumFacing side) {
            return BCTransportModels.BAKER_PLUG_BLOCKER.bake(new KeyPlugBlocker(side));
        }
    }
}
