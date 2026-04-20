package ct.buildcraft.builders.snapshot.pattern;

import java.util.BitSet;

import ct.buildcraft.api.core.render.ISprite;
import ct.buildcraft.api.filler.IFilledTemplate;
import ct.buildcraft.api.filler.IFillerPatternShape;
import ct.buildcraft.api.statements.IStatementParameter;
import ct.buildcraft.builders.BCBuildersSprites;
import ct.buildcraft.builders.snapshot.Snapshot;
import ct.buildcraft.builders.snapshot.pattern.parameter.PatternParameterHollow;

public class PatternSphere extends Pattern implements IFillerPatternShape {
    public PatternSphere() {
        super("sphere");
    }

    @Override
    public ISprite getSprite() {
        return BCBuildersSprites.FILLER_SPHERE;
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
    public boolean fillTemplate(IFilledTemplate filledTemplate, IStatementParameter[] params) {
        PatternParameterHollow hollow = getParam(0, params, PatternParameterHollow.FILLED_INNER);

        double cx = filledTemplate.getMax().getX() / 2.0;
        double cy = filledTemplate.getMax().getY() / 2.0;
        double cz = filledTemplate.getMax().getZ() / 2.0;

        double rx = cx + 0.5;
        double ry = cy + 0.5;
        double rz = cz + 0.5;

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
                    for (int z = 0; z <= filledTemplate.getMax().getZ(); z++) {
                        if (data.get(Snapshot.posToIndex(filledTemplate.getSize(), x, y, z))) {
                            filledTemplate.set(x, y, z, true);
                            break;
                        }
                        if (outerFilled) {
                            filledTemplate.set(x, y, z, true);
                        }
                    }

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

            // Y iteration
            for (int x = 0; x <= filledTemplate.getMax().getX(); x++) {
                for (int z = 0; z <= filledTemplate.getMax().getZ(); z++) {
                    for (int y = 0; y <= filledTemplate.getMax().getY(); y++) {
                        if (data.get(Snapshot.posToIndex(filledTemplate.getSize(), x, y, z))) {
                            filledTemplate.set(x, y, z, true);
                            break;
                        }
                        if (outerFilled) {
                            filledTemplate.set(x, y, z, true);
                        }
                    }

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

            // X iteration
            for (int y = 0; y <= filledTemplate.getMax().getY(); y++) {
                for (int z = 0; z <= filledTemplate.getMax().getZ(); z++) {
                    for (int x = 0; x <= filledTemplate.getMax().getX(); x++) {
                        if (data.get(Snapshot.posToIndex(filledTemplate.getSize(), x, y, z))) {
                            filledTemplate.set(x, y, z, true);
                            break;
                        }
                        if (outerFilled) {
                            filledTemplate.set(x, y, z, true);
                        }
                    }

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
        return true;
    }
}
