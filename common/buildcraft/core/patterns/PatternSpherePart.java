package buildcraft.core.patterns;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.Vec3d;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.filler.FilledTemplate;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.containers.IFillerStatementContainer;

import buildcraft.lib.misc.VecUtil;

import buildcraft.core.BCCoreSprites;

public final class PatternSpherePart extends Pattern {

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
        return BCCoreSprites.FILLER_SPHERE_PART.get(type);
    }

    @Override
    public FilledTemplate createTemplate(IFillerStatementContainer filler, IStatementParameter[] params) {
        PatternParameterHollow hollow = getParam(0, params, PatternParameterHollow.FILLED_INNER);
        PatternParameterFacing facing = getParam(1, params, PatternParameterFacing.DOWN);
        PatternParameterRotation rotation = getParam(2, params, PatternParameterRotation.NONE);

        FilledTemplate tpl = new FilledTemplate(filler.getBox());

        Vec3d center;
        Vec3d radius;

        Set<EnumFacing> innerSides = EnumSet.noneOf(EnumFacing.class);

        Vec3d max = new Vec3d(tpl.maxX, tpl.maxY, tpl.maxZ);
        center = VecUtil.scale(max, 0.5);
        radius = center.addVector(0.5, 0.5, 0.5);

        innerSides.add(facing.face);

        Axis axis = facing.face.getAxis();
        Vec3d offset = VecUtil.offset(Vec3d.ZERO, facing.face, VecUtil.getValue(radius, axis));
        center = center.add(offset);
        radius = VecUtil.replaceValue(radius, axis, VecUtil.getValue(radius, axis) * 2);

        if (type.openFaces > 1) {
            Axis secondaryAxis;
            if (rotation.rotationCount % 2 == 1) {
                secondaryAxis = axis == Axis.X ? Axis.Y : axis == Axis.Y ? Axis.Z : Axis.X;
            } else {
                secondaryAxis = axis == Axis.X ? Axis.Z : axis == Axis.Y ? Axis.X : Axis.Y;
            }
            EnumFacing secondaryFace = VecUtil.getFacing(secondaryAxis, rotation.rotationCount >= 2);
            innerSides.add(secondaryFace);

            offset = VecUtil.offset(Vec3d.ZERO, secondaryFace, VecUtil.getValue(radius, secondaryAxis));
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
                EnumFacing tertiaryFace = VecUtil.getFacing(tertiaryAxis, rotationCount >= 2);
                innerSides.add(tertiaryFace);

                offset = VecUtil.offset(Vec3d.ZERO, tertiaryFace, VecUtil.getValue(radius, tertiaryAxis));
                center = center.add(offset);
                radius = VecUtil.replaceValue(radius, tertiaryAxis, VecUtil.getValue(radius, tertiaryAxis) * 2);
            }
        }

        double cx = center.xCoord;
        double cy = center.yCoord;
        double cz = center.zCoord;

        double rx = radius.xCoord;
        double ry = radius.yCoord;
        double rz = radius.zCoord;

        for (int x = 0; x <= tpl.maxX; x++) {
            double dx = Math.abs(x - cx) / rx;
            double dxx = dx * dx;
            for (int y = 0; y <= tpl.maxY; y++) {
                double dy = Math.abs(y - cy) / ry;
                double dyy = dy * dy;
                for (int z = 0; z <= tpl.maxZ; z++) {
                    double dz = Math.abs(z - cz) / rz;
                    double dzz = dz * dz;
                    if (dxx + dyy + dzz < 1) {
                        tpl.fill(x, y, z);
                    }
                }
            }
        }

        boolean removeInside = hollow != PatternParameterHollow.FILLED_INNER;
        boolean outerFilled = hollow.outerFilled;

        if (removeInside) {
            FilledTemplate tpl2 = new FilledTemplate(filler.getBox());

            // Z iteration
            for (int x = 0; x <= tpl.maxX; x++) {
                for (int y = 0; y <= tpl.maxY; y++) {
                    if (!innerSides.contains(EnumFacing.NORTH)) {
                        for (int z = 0; z <= tpl.maxZ; z++) {
                            if (tpl.shouldFill(x, y, z)) {
                                tpl2.fill(x, y, z);
                                break;
                            }
                            if (outerFilled) {
                                tpl2.fill(x, y, z);
                            }
                        }
                    }

                    if (!innerSides.contains(EnumFacing.SOUTH)) {
                        for (int z = tpl.maxZ; z >= 0; z--) {
                            if (tpl.shouldFill(x, y, z)) {
                                tpl2.fill(x, y, z);
                                break;
                            }
                            if (outerFilled) {
                                tpl2.fill(x, y, z);
                            }
                        }
                    }
                }
            }

            // Y iteration
            for (int x = 0; x <= tpl.maxX; x++) {
                for (int z = 0; z <= tpl.maxZ; z++) {
                    if (!innerSides.contains(EnumFacing.DOWN)) {
                        for (int y = 0; y <= tpl.maxY; y++) {
                            if (tpl.shouldFill(x, y, z)) {
                                tpl2.fill(x, y, z);
                                break;
                            }
                            if (outerFilled) {
                                tpl2.fill(x, y, z);
                            }
                        }
                    }

                    if (!innerSides.contains(EnumFacing.UP)) {
                        for (int y = tpl.maxY; y >= 0; y--) {
                            if (tpl.shouldFill(x, y, z)) {
                                tpl2.fill(x, y, z);
                                break;
                            }
                            if (outerFilled) {
                                tpl2.fill(x, y, z);
                            }
                        }
                    }
                }
            }

            // X iteration
            for (int y = 0; y <= tpl.maxY; y++) {
                for (int z = 0; z <= tpl.maxZ; z++) {
                    if (!innerSides.contains(EnumFacing.WEST)) {
                        for (int x = 0; x <= tpl.maxX; x++) {
                            if (tpl.shouldFill(x, y, z)) {
                                tpl2.fill(x, y, z);
                                break;
                            }
                            if (outerFilled) {
                                tpl2.fill(x, y, z);
                            }
                        }
                    }

                    if (!innerSides.contains(EnumFacing.EAST)) {
                        for (int x = tpl.maxX; x >= 0; x--) {
                            if (tpl.shouldFill(x, y, z)) {
                                tpl2.fill(x, y, z);
                                break;
                            }
                            if (outerFilled) {
                                tpl2.fill(x, y, z);
                            }
                        }
                    }
                }
            }

            tpl = tpl2;
        }

        return tpl;

    }
}
