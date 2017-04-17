package buildcraft.transport.client.model.plug;

import buildcraft.api.transport.pluggable.IPluggableStaticBaker;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.model.MutableVertex;
import buildcraft.transport.client.model.key.KeyPlugFacade;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public enum PlugBakerFacade implements IPluggableStaticBaker<KeyPlugFacade> {
    INSTANCE;

    private List<BakedQuad> getTransormedQuads(IBlockState state, IBakedModel model, EnumFacing side,
                                               Vec3d pos0, Vec3d pos1, Vec3d pos2, Vec3d pos3) {
        return model.getQuads(state, side, 0).stream()
                .map(quad -> {
                    MutableQuad mutableQuad = new MutableQuad().fromBakedItem(quad);
                    boolean positive = side.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE;
                    BiConsumer<MutableVertex, Vec3d> setPosition = (vertex, pos) -> {
                        switch (side.getAxis()) {
                            case X:
                                vertex.positiond(
                                        positive ? 1 - pos.zCoord : pos.zCoord,
                                        pos.yCoord,
                                        pos.xCoord
                                );
                                break;
                            case Y:
                                vertex.positiond(
                                        pos.xCoord,
                                        positive ? 1 - pos.zCoord : pos.zCoord,
                                        pos.yCoord
                                );
                                break;
                            case Z:
                                vertex.positiond(
                                        pos.yCoord,
                                        pos.xCoord,
                                        positive ? 1 - pos.zCoord : pos.zCoord
                                );
                                break;
                        }
                    };
                    setPosition.accept(mutableQuad.vertex_0, positive ? pos3 : pos0);
                    setPosition.accept(mutableQuad.vertex_1, positive ? pos2 : pos1);
                    setPosition.accept(mutableQuad.vertex_2, positive ? pos1 : pos2);
                    setPosition.accept(mutableQuad.vertex_3, positive ? pos0 : pos3);
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

    private void addRotateQuads(List<BakedQuad> quads, IBlockState state, IBakedModel model, EnumFacing side, Rotation rotation,
                                Vec3d pos0, Vec3d pos1, Vec3d pos2, Vec3d pos3) {
        quads.addAll(getTransormedQuads(
                state, model, side,
                rotation.ordinal() % 2 != 0 ? rotate(pos0, rotation) : rotate(pos3, rotation),
                rotation.ordinal() % 2 != 0 ? rotate(pos1, rotation) : rotate(pos2, rotation),
                rotation.ordinal() % 2 != 0 ? rotate(pos2, rotation) : rotate(pos1, rotation),
                rotation.ordinal() % 2 != 0 ? rotate(pos3, rotation) : rotate(pos0, rotation)
        ));
    }

    @Override
    public List<BakedQuad> bake(KeyPlugFacade key) {
        IBlockState state = Blocks.GRASS.getDefaultState();
        IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(state);
        List<BakedQuad> quads = new ArrayList<>();
        for (Rotation rotation : Rotation.values()) {
            addRotateQuads(
                    quads, state, model, key.side, rotation,
                    new Vec3d(0 / 16D, 16 / 16D, 0 / 16D),
                    new Vec3d(4 / 16D, 16 / 16D, 0 / 16D),
                    new Vec3d(4 / 16D, 0 / 16D, 0 / 16D),
                    new Vec3d(0 / 16D, 0 / 16D, 0 / 16D)
            );
            addRotateQuads(
                    quads, state, model, key.side.getOpposite(), rotation,
                    new Vec3d(0 / 16D, 16 / 16D, 16 / 16D),
                    new Vec3d(1 / 16D, 15 / 16D, 15 / 16D),
                    new Vec3d(1 / 16D, 1 / 16D, 15 / 16D),
                    new Vec3d(0 / 16D, 0 / 16D, 16 / 16D)
            );
            addRotateQuads(
                    quads, state, model, key.side.getOpposite(), rotation,
                    new Vec3d(1 / 16D, 15 / 16D, 15 / 16D),
                    new Vec3d(4 / 16D, 15 / 16D, 15 / 16D),
                    new Vec3d(4 / 16D, 1 / 16D, 15 / 16D),
                    new Vec3d(1 / 16D, 1 / 16D, 15 / 16D)
            );
        }
        for (EnumFacing facing : EnumFacing.values()) {
            if (facing.getAxis() != key.side.getAxis()) {
                boolean positive = key.side.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE;
                if (key.side.getAxis() == EnumFacing.Axis.Z && facing.getAxis() == EnumFacing.Axis.X ||
                        key.side.getAxis() == EnumFacing.Axis.X && facing.getAxis() == EnumFacing.Axis.Y ||
                        key.side.getAxis() == EnumFacing.Axis.Y && facing.getAxis() == EnumFacing.Axis.Z) {
                    quads.addAll(getTransormedQuads(
                            state, model, facing,
                            new Vec3d(positive ? 16 / 16D : 1 / 16D, 4 / 16D, 12 / 16D),
                            new Vec3d(positive ? 16 / 16D : 1 / 16D, 12 / 16D, 12 / 16D),
                            new Vec3d(positive ? 15 / 16D : 0 / 16D, 12 / 16D, 12 / 16D),
                            new Vec3d(positive ? 15 / 16D : 0 / 16D, 4 / 16D, 12 / 16D)
                    ));
                } else {
                    quads.addAll(getTransormedQuads(
                            state, model, facing,
                            new Vec3d(4 / 16D, positive ? 16 / 16D : 1 / 16D, 12 / 16D),
                            new Vec3d(4 / 16D, positive ? 15 / 16D : 0 / 16D, 12 / 16D),
                            new Vec3d(12 / 16D, positive ? 15 / 16D : 0 / 16D, 12 / 16D),
                            new Vec3d(12 / 16D, positive ? 16 / 16D : 1 / 16D, 12 / 16D)
                    ));
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
