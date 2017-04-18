package buildcraft.transport.client.model.plug;

import buildcraft.api.transport.pluggable.IPluggableStaticBaker;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.model.MutableVertex;
import buildcraft.lib.misc.VecUtil;
import buildcraft.transport.client.model.key.KeyPlugFacade;
import buildcraft.transport.plug.PluggableFacade;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum PlugBakerFacade implements IPluggableStaticBaker<KeyPlugFacade> {
    INSTANCE;

    private int getVertexIndex(List<Vec3d> positions, EnumFacing.Axis axis,
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
                        .orElseThrow(AssertionError::new)
        );
    }

    private List<BakedQuad> getTransformedQuads(IBlockState state, IBakedModel model, EnumFacing side,
                                                Vec3d pos0, Vec3d pos1, Vec3d pos2, Vec3d pos3) {
        return model.getQuads(state, side, 0).stream()
                .map(quad -> {
                    MutableQuad mutableQuad = new MutableQuad().fromBakedItem(quad);
                    boolean positive = side.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE;
                    Function<Vec3d, Vec3d> transformPosition = pos -> {
                        switch (side.getAxis()) {
                            case X:
                                return new Vec3d(
                                        positive ? 1 - pos.zCoord : pos.zCoord,
                                        pos.yCoord,
                                        pos.xCoord
                                );
                            case Y:
                                return new Vec3d(
                                        pos.xCoord,
                                        positive ? 1 - pos.zCoord : pos.zCoord,
                                        pos.yCoord
                                );
                            case Z:
                                return new Vec3d(
                                        pos.yCoord,
                                        pos.xCoord,
                                        positive ? 1 - pos.zCoord : pos.zCoord
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
                        vertex.positiond(newPos.xCoord, newPos.yCoord, newPos.zCoord);
                        switch (side.getAxis()) {
                            case X:
                                vertex.texf(
                                        (float) (minU + (maxU - minU) * newPos.zCoord),
                                        (float) (minV + (maxV - minV) * (1 - newPos.yCoord))
                                );
                                break;
                            case Y:
                                vertex.texf(
                                        (float) (minU + (maxU - minU) * newPos.xCoord),
                                        (float) (minV + (maxV - minV) * (1 - newPos.zCoord))
                                );
                                break;
                            case Z:
                                vertex.texf(
                                        (float) (minU + (maxU - minU) * newPos.xCoord),
                                        (float) (minV + (maxV - minV) * (1 - newPos.yCoord))
                                );
                                break;
                        }
                    });
                    return mutableQuad.toBakedItem();
                })
                .collect(Collectors.toList());
    }

    private Vec3d rotate(Vec3d vec, Rotation rotation) {
        switch (rotation) {
            case NONE:
                return new Vec3d(vec.xCoord, vec.yCoord, vec.zCoord);
            case CLOCKWISE_90:
                return new Vec3d(1 - vec.yCoord, 1 - vec.xCoord, vec.zCoord);
            case CLOCKWISE_180:
                return new Vec3d(1 - vec.xCoord, 1 - vec.yCoord, vec.zCoord);
            case COUNTERCLOCKWISE_90:
                return new Vec3d(vec.yCoord, vec.xCoord, vec.zCoord);
        }
        throw new IllegalArgumentException();
    }

    private void addRotatedQuads(List<BakedQuad> quads, IBlockState state, IBakedModel model, EnumFacing side, Rotation rotation,
                                 Vec3d pos0, Vec3d pos1, Vec3d pos2, Vec3d pos3) {
        quads.addAll(getTransformedQuads(
                state, model, side,
                rotate(pos0, rotation),
                rotate(pos1, rotation),
                rotate(pos2, rotation),
                rotate(pos3, rotation)
        ));
    }

    @Override
    public List<BakedQuad> bake(KeyPlugFacade key) {
        IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(key.state);
        List<BakedQuad> quads = new ArrayList<>();
        for (Rotation rotation : Rotation.values()) {
            addRotatedQuads(
                    quads, key.state, model, key.side, rotation,
                    new Vec3d(0 / 16D, 16 / 16D, 0 / 16D),
                    new Vec3d(key.isHollow ? 4 / 16D : 8 / 16D, 16 / 16D, 0 / 16D),
                    new Vec3d(key.isHollow ? 4 / 16D : 8 / 16D, 0 / 16D, 0 / 16D),
                    new Vec3d(0 / 16D, 0 / 16D, 0 / 16D)
            );
            addRotatedQuads(
                    quads, key.state, model, key.side.getOpposite(), rotation,
                    new Vec3d(0 / 16D, 16 / 16D, 16 / 16D),
                    new Vec3d(PluggableFacade.SIZE / 16D, (16 - PluggableFacade.SIZE) / 16D, (16 - PluggableFacade.SIZE) / 16D),
                    new Vec3d(PluggableFacade.SIZE / 16D, PluggableFacade.SIZE / 16D, (16 - PluggableFacade.SIZE) / 16D),
                    new Vec3d(0 / 16D, 0 / 16D, 16 / 16D)
            );
            addRotatedQuads(
                    quads, key.state, model, key.side.getOpposite(), rotation,
                    new Vec3d(PluggableFacade.SIZE / 16D, (16 - PluggableFacade.SIZE) / 16D, (16 - PluggableFacade.SIZE) / 16D),
                    new Vec3d(key.isHollow ? 4 / 16D : 8 / 16D, (16 - PluggableFacade.SIZE) / 16D, (16 - PluggableFacade.SIZE) / 16D),
                    new Vec3d(key.isHollow ? 4 / 16D : 8 / 16D, PluggableFacade.SIZE / 16D, (16 - PluggableFacade.SIZE) / 16D),
                    new Vec3d(PluggableFacade.SIZE / 16D, PluggableFacade.SIZE / 16D, (16 - PluggableFacade.SIZE) / 16D)
            );
        }
        if (key.isHollow) {
            for (EnumFacing facing : EnumFacing.values()) {
                if (facing.getAxis() != key.side.getAxis()) {
                    boolean positive = key.side.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE;
                    if (key.side.getAxis() == EnumFacing.Axis.Z && facing.getAxis() == EnumFacing.Axis.X ||
                            key.side.getAxis() == EnumFacing.Axis.X && facing.getAxis() == EnumFacing.Axis.Y ||
                            key.side.getAxis() == EnumFacing.Axis.Y && facing.getAxis() == EnumFacing.Axis.Z) {
                        quads.addAll(getTransformedQuads(
                                key.state, model, facing,
                                new Vec3d(positive ? 16 / 16D : PluggableFacade.SIZE / 16D, 4 / 16D, 12 / 16D),
                                new Vec3d(positive ? 16 / 16D : PluggableFacade.SIZE / 16D, 12 / 16D, 12 / 16D),
                                new Vec3d(positive ? (16 - PluggableFacade.SIZE) / 16D : 0 / 16D, 12 / 16D, 12 / 16D),
                                new Vec3d(positive ? (16 - PluggableFacade.SIZE) / 16D : 0 / 16D, 4 / 16D, 12 / 16D)
                        ));
                    } else {
                        quads.addAll(getTransformedQuads(
                                key.state, model, facing,
                                new Vec3d(4 / 16D, positive ? 16 / 16D : PluggableFacade.SIZE / 16D, 12 / 16D),
                                new Vec3d(4 / 16D, positive ? (16 - PluggableFacade.SIZE) / 16D : 0 / 16D, 12 / 16D),
                                new Vec3d(12 / 16D, positive ? (16 - PluggableFacade.SIZE) / 16D : 0 / 16D, 12 / 16D),
                                new Vec3d(12 / 16D, positive ? 16 / 16D : PluggableFacade.SIZE / 16D, 12 / 16D)
                        ));
                    }
                }
            }
        }
        return quads.stream()
                .map(quad ->
                        quad.hasTintIndex()
                                ? new MutableQuad()
                                .fromBakedItem(quad)
                                .setTint(quad.getTintIndex() * 6 + key.side.ordinal())
                                .toBakedItem()
                                : quad
                )
                .collect(Collectors.toList());
    }
}
