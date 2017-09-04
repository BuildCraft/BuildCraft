package buildcraft.core.builders.patterns;

import net.minecraft.util.math.Vec3d;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.render.ISprite;
import buildcraft.api.filler.FilledTemplate;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.containers.IFillerStatementContainer;

import buildcraft.core.BCCoreSprites;

public class PatternSphere extends Pattern {
    public PatternSphere() {
        super("sphere");
    }

    @Override
    public ISprite getSprite() {
        return BCCoreSprites.FILLER_SPHERE;
    }

    @Override
    public int minParameters() {
        return 1;
    }

    @Override
    public int maxParameters() {
        return 1;
    }

    @Override
    public IStatementParameter createParameter(int index) {
        switch (index) {
            case 0:
                return PatternParameterHollow.FILLED_INNER;
            default:
                return null;
        }
    }

    @Override
    public FilledTemplate createTemplate(IFillerStatementContainer filler, IStatementParameter[] params) {
        PatternParameterHollow hollow = getParam(0, params, PatternParameterHollow.FILLED_INNER);

        FilledTemplate tpl = new FilledTemplate(filler.getBox());

        double cx = tpl.maxX / 2.0;
        double cy = tpl.maxY / 2.0;
        double cz = tpl.maxZ / 2.0;

        double rx = cx + 0.5;
        double ry = cy + 0.5;
        double rz = cz + 0.5;

        BCLog.logger.info("PatternSphere: c = " + new Vec3d(cx, cy, cz).add(new Vec3d(tpl.min)) + ", r = " + new Vec3d(rx, ry, rz));

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
                    for (int z = 0; z <= tpl.maxZ; z++) {
                        if (tpl.get(x, y, z)) {
                            tpl2.fill(x, y, z);
                            break;
                        }
                        if (outerFilled) {
                            tpl2.fill(x, y, z);
                        }
                    }

                    for (int z = tpl.maxZ; z >= 0; z--) {
                        if (tpl.get(x, y, z)) {
                            tpl2.fill(x, y, z);
                            break;
                        }
                        if (outerFilled) {
                            tpl2.fill(x, y, z);
                        }
                    }
                }
            }
            
            // Y iteration
            for (int x = 0; x <= tpl.maxX; x++) {
                for (int z = 0; z <= tpl.maxZ; z++) {
                    for (int y = 0; y <= tpl.maxY; y++) {
                        if (tpl.get(x, y, z)) {
                            tpl2.fill(x, y, z);
                            break;
                        }
                        if (outerFilled) {
                            tpl2.fill(x, y, z);
                        }
                    }

                    for (int y = tpl.maxY; y >= 0; y--) {
                        if (tpl.get(x, y, z)) {
                            tpl2.fill(x, y, z);
                            break;
                        }
                        if (outerFilled) {
                            tpl2.fill(x, y, z);
                        }
                    }
                }
            }

            // X iteration
            for (int y = 0; y <= tpl.maxY; y++) {
                for (int z = 0; z <= tpl.maxZ; z++) {
                    for (int x = 0; x <= tpl.maxX; x++) {
                        if (tpl.get(x, y, z)) {
                            tpl2.fill(x, y, z);
                            break;
                        }
                        if (outerFilled) {
                            tpl2.fill(x, y, z);
                        }
                    }

                    for (int x = tpl.maxX; x >= 0; x--) {
                        if (tpl.get(x, y, z)) {
                            tpl2.fill(x, y, z);
                            break;
                        }
                        if (outerFilled) {
                            tpl2.fill(x, y, z);
                        }
                    }
                }
            }
            
            tpl = tpl2;
        }

        return tpl;
    }
}
