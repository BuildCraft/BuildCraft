package ct.buildcraft.builders.snapshot.pattern;

import java.util.BitSet;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

import ct.buildcraft.api.core.render.ISprite;
import ct.buildcraft.api.filler.IFilledTemplate;
import ct.buildcraft.api.filler.IFillerPatternShape;
import ct.buildcraft.api.statements.IStatementParameter;
import ct.buildcraft.builders.BCBuildersSprites;
import ct.buildcraft.builders.snapshot.Snapshot;
import ct.buildcraft.builders.snapshot.pattern.parameter.PatternParameterFacing;
import ct.buildcraft.builders.snapshot.pattern.parameter.PatternParameterHollow;
import ct.buildcraft.builders.snapshot.pattern.parameter.PatternParameterRotation;
import ct.buildcraft.lib.misc.VecUtil;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.phys.Vec3;

public final class PatternSpherePart extends Pattern implements IFillerPatternShape {
    public enum SpherePartType {
        EIGHTH(3),
        QUARTER(2),
        HALF(1);

        public final String lowerCaseName = name().toLowerCase(Locale.ROOT);
        final int openFaces;

        SpherePartType(int numOpenFaces) {
            this.openFaces = numOpenFaces;
        }
    }

    private final SpherePartType type;

    public PatternSpherePart(SpherePartType type) {
        super("sphere_" + type.lowerCaseName);
        this.type = type;
    }

    @Override
    public int minParameters() {
        return type.openFaces == 1 ? 2 : 3;
    }

    @Override
    public int maxParameters() {
        return minParameters();
    }

    @Override
    public IStatementParameter createParameter(int index) {
        if (index >= minParameters()) {
            return null;
        }
        switch (index) {
            case 0:
                return PatternParameterHollow.FILLED_INNER;
            case 1:
                return PatternParameterFacing.DOWN;
            case 2:
                return PatternParameterRotation.NONE;
            default:
                return null;
        }
    }

    @Override
    public ISprite getSprite() {
        return BCBuildersSprites.FILLER_SPHERE_PART.get(type);
    }

    @Override
    public boolean fillTemplate(IFilledTemplate filledTemplate, IStatementParameter[] params) {
        PatternParameterFacing facing = getParam(1, params, PatternParameterFacing.DOWN);
        PatternParameterRotation rotation = getParam(2, params, PatternParameterRotation.NONE);
        PatternParameterHollow hollow = getParam(0, params, PatternParameterHollow.FILLED_INNER);

        Vec3 center;
        Vec3 radius;

        Set<Direction> innerSides = EnumSet.noneOf(Direction.class);

        Vec3 max = new Vec3(filledTemplate.getMax().getX(), filledTemplate.getMax().getY(), filledTemplate.getMax().getZ());
        center = VecUtil.scale(max, 0.5);
        radius = center.add(0.5, 0.5, 0.5);

        innerSides.add(facing.face);

        Axis axis = facing.face.getAxis();
        Vec3 offset = VecUtil.offset(Vec3.ZERO, facing.face, VecUtil.getValue(radius, axis));
        center = center.add(offset);
        radius = VecUtil.replaceValue(radius, axis, VecUtil.getValue(radius, axis) * 2);

        if (type.openFaces > 1) {
            Axis secondaryAxis;
            if (rotation.rotationCount % 2 == 1) {
                secondaryAxis = axis == Axis.X ? Axis.Y : axis == Axis.Y ? Axis.Z : Axis.X;
            } else {
                secondaryAxis = axis == Axis.X ? Axis.Z : axis == Axis.Y ? Axis.X : Axis.Y;
            }
            Direction secondaryFace = VecUtil.getFacing(secondaryAxis, rotation.rotationCount >= 2);
            innerSides.add(secondaryFace);

            offset = VecUtil.offset(Vec3.ZERO, secondaryFace, VecUtil.getValue(radius, secondaryAxis));
            center = center.add(offset);
            radius = VecUtil.replaceValue(radius, secondaryAxis, VecUtil.getValue(radius, secondaryAxis) * 2);

            if (type.openFaces > 2) {
                Axis tertiaryAxis;
                int rotationCount = (rotation.rotationCount + 1) & 3;
                if (rotationCount % 2 == 1) {
                    tertiaryAxis = axis == Axis.X ? Axis.Y : axis == Axis.Y ? Axis.Z : Axis.X;
                } else {
                    tertiaryAxis = axis == Axis.X ? Axis.Z : axis == Axis.Y ? Axis.X : Axis.Y;
                }
                Direction tertiaryFace = VecUtil.getFacing(tertiaryAxis, rotationCount >= 2);
                innerSides.add(tertiaryFace);

                offset = VecUtil.offset(Vec3.ZERO, tertiaryFace, VecUtil.getValue(radius, tertiaryAxis));
                center = center.add(offset);
                radius = VecUtil.replaceValue(radius, tertiaryAxis, VecUtil.getValue(radius, tertiaryAxis) * 2);
            }
        }

        double cx = center.x;
        double cy = center.y;
        double cz = center.z;

        double rx = radius.x;
        double ry = radius.y;
        double rz = radius.z;

        BitSet data = null;
        if (hollow != PatternParameterHollow.FILLED_INNER) {
            data = new BitSet(Snapshot.getDataSize(filledTemplate.getSize()));
        }
        for (int x = 0; x <= filledTemplate.getMax().getX(); x++) {
            double dx = Math.abs(x - cx) / rx;
            double dxx = dx * dx;
            for (int y = 0; y <= filledTemplate.getMax().getY(); y++) {
                double dy = Math.abs(y - cy) / ry;
                double dyy = dy * dy;
                for (int z = 0; z <= filledTemplate.getMax().getZ(); z++) {
                    double dz = Math.abs(z - cz) / rz;
                    double dzz = dz * dz;
                    if (dxx + dyy + dzz < 1) {
                        if (hollow != PatternParameterHollow.FILLED_INNER) {
                            // noinspection ConstantConditions
                            data.set(Snapshot.posToIndex(filledTemplate.getSize(), x, y, z), true);
                        } else {
                            filledTemplate.set(x, y, z, true);
                        }
                    }
                }
            }
        }

        boolean outerFilled = hollow.outerFilled;

        if (hollow != PatternParameterHollow.FILLED_INNER) {
            // Z iteration
            for (int x = 0; x <= filledTemplate.getMax().getX(); x++) {
                for (int y = 0; y <= filledTemplate.getMax().getY(); y++) {
                    if (!innerSides.contains(Direction.NORTH)) {
                        for (int z = 0; z <= filledTemplate.getMax().getZ(); z++) {
                            if (data.get(Snapshot.posToIndex(filledTemplate.getSize(), x, y, z))) {
                                filledTemplate.set(x, y, z, true);
                                break;
                            }
                            if (outerFilled) {
                                filledTemplate.set(x, y, z, true);
                            }
                        }
                    }

                    if (!innerSides.contains(Direction.SOUTH)) {
                        for (int z = filledTemplate.getMax().getZ(); z >= 0; z--) {
                            if (data.get(Snapshot.posToIndex(filledTemplate.getSize(), x, y, z))) {
                                filledTemplate.set(x, y, z, true);
                                break;
                            }
                            if (outerFilled) {
                                filledTemplate.set(x, y, z, true);
                            }
                        }
                    }
                }
            }

            // Y iteration
            for (int x = 0; x <= filledTemplate.getMax().getX(); x++) {
                for (int z = 0; z <= filledTemplate.getMax().getZ(); z++) {
                    if (!innerSides.contains(Direction.DOWN)) {
                        for (int y = 0; y <= filledTemplate.getMax().getY(); y++) {
                            if (data.get(Snapshot.posToIndex(filledTemplate.getSize(), x, y, z))) {
                                filledTemplate.set(x, y, z, true);
                                break;
                            }
                            if (outerFilled) {
                                filledTemplate.set(x, y, z, true);
                            }
                        }
                    }

                    if (!innerSides.contains(Direction.UP)) {
                        for (int y = filledTemplate.getMax().getY(); y >= 0; y--) {
                            if (data.get(Snapshot.posToIndex(filledTemplate.getSize(), x, y, z))) {
                                filledTemplate.set(x, y, z, true);
                                break;
                            }
                            if (outerFilled) {
                                filledTemplate.set(x, y, z, true);
                            }
                        }
                    }
                }
            }

            // X iteration
            for (int y = 0; y <= filledTemplate.getMax().getY(); y++) {
                for (int z = 0; z <= filledTemplate.getMax().getZ(); z++) {
                    if (!innerSides.contains(Direction.WEST)) {
                        for (int x = 0; x <= filledTemplate.getMax().getX(); x++) {
                            if (data.get(Snapshot.posToIndex(filledTemplate.getSize(), x, y, z))) {
                                filledTemplate.set(x, y, z, true);
                                break;
                            }
                            if (outerFilled) {
                                filledTemplate.set(x, y, z, true);
                            }
                        }
                    }

                    if (!innerSides.contains(Direction.EAST)) {
                        for (int x = filledTemplate.getMax().getX(); x >= 0; x--) {
                            if (data.get(Snapshot.posToIndex(filledTemplate.getSize(), x, y, z))) {
                                filledTemplate.set(x, y, z, true);
                                break;
                            }
                            if (outerFilled) {
                                filledTemplate.set(x, y, z, true);
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
}
