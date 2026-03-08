package buildcraft.builders.snapshot.pattern;

import buildcraft.api.filler.IFilledTemplate;
import buildcraft.api.filler.IFillerPatternShape;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.builders.snapshot.pattern.parameter.PatternParameterAxis;
import buildcraft.builders.snapshot.pattern.parameter.PatternParameterHollow;
import buildcraft.builders.snapshot.pattern.parameter.PatternParameterRotation;
import buildcraft.lib.misc.PositionUtil;
import buildcraft.lib.misc.PositionUtil.PathIterator2d;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class PatternShape2d extends Pattern implements IFillerPatternShape {
    public PatternShape2d(String tag) {
        super(tag);
    }

    @Override
    public int minParameters() {
        return 3;
    }

    @Override
    public int maxParameters() {
        return 3;
    }

    @Override
    public IStatementParameter createParameter(int index) {
        switch (index) {
            case 0:
                return PatternParameterAxis.Y;
            case 1:
                return PatternParameterHollow.HOLLOW;
            case 2:
                return PatternParameterRotation.NONE;
        }
        return null;
    }

    @Override
    public boolean fillTemplate(IFilledTemplate filledTemplate, IStatementParameter[] params) {
        PatternParameterAxis axis = getParam(0, params, PatternParameterAxis.Y);
        PatternParameterRotation dir = getParam(2, params, PatternParameterRotation.NONE);

        PathIterator2d iterator = getIterator(filledTemplate, axis);

        int maxA = axis == PatternParameterAxis.X ? filledTemplate.getMax().getY() : filledTemplate.getMax().getX();
        int maxB = axis == PatternParameterAxis.Z ? filledTemplate.getMax().getY() : filledTemplate.getMax().getZ();

        int normMaxA = maxA;
        int normMaxB = maxB;

        if (dir.rotationCount % 2 == 1) {
            int maxT = maxA;
            maxA = maxB;
            maxB = maxT;
            final int max_b = maxB;
            final PathIterator2d old = iterator;
            iterator = (a, b) -> old.iterate(max_b - b, a);
        }
        if (dir.rotationCount > 1) {
            final PathIterator2d old = iterator;
            final int max_a = maxA;
            final int max_b = maxB;
            // Technically this is wrong... but it doesn't matter for any of
            // the shapes that use this, as they are all symetrical
            iterator = (a, b) -> old.iterate(max_a - a, max_b - b);
        }

        LineList list = new LineList(iterator);
        genShape(maxA, maxB, list);

        PatternParameterHollow filled = getParam(1, params, PatternParameterHollow.HOLLOW);
        if (filled.filled) {
            int fillA = list.fillInA;
            int fillB = list.fillInB;
            if (fillA != -1 && fillB != -1) {
                maxA = normMaxA;
                maxB = normMaxB;

                if (dir.rotationCount % 2 == 1) {
                    int fillT = fillA;
                    fillA = maxB - fillB;
                    fillB = fillT;
                }
                if (dir.rotationCount > 1) {
                    fillA = maxA - fillA;
                    fillB = maxB - fillB;
                }
                iterator = getIterator(filledTemplate, axis);
                PositionGetter getter = getFillGetter(filledTemplate, axis);

                if (filled.outerFilled) {
                    iterator = (a, b) ->
                    {
                    };
                }

                // Expand outwards from the point
                Set<Point> visited = new HashSet<>();
                List<Point> open = new ArrayList<>();
                open.add(new Point(fillA, fillB));
                while (!open.isEmpty()) {
                    List<Point> next = new ArrayList<>();
                    for (Point p : open) {
                        if (p.a < 0 || p.a > maxA) {
                            continue;
                        }
                        if (p.b < 0 || p.b > maxB) {
                            continue;
                        }
                        if (!visited.add(p)) {
                            continue;
                        }
                        if (getter.isFilled(p.a, p.b)) {
                            continue;
                        }
                        iterator.iterate(p.a, p.b);
                        next.add(new Point(p.a + 1, p.b));
                        next.add(new Point(p.a - 1, p.b));
                        next.add(new Point(p.a, p.b + 1));
                        next.add(new Point(p.a, p.b - 1));
                    }
                    open = next;
                }

                if (filled.outerFilled) {
                    iterator = getIterator(filledTemplate, axis);
                    for (int a = 0; a <= maxA; a++) {
                        for (int b = 0; b <= maxB; b++) {
                            if (visited.contains(new Point(a, b))) {
                                continue;
                            }
                            iterator.iterate(a, b);
                        }
                    }
                }
            }
        }
        return true;
    }

    @FunctionalInterface
    interface PositionGetter {
        boolean isFilled(int a, int b);
    }

    private static PathIterator2d getIterator(IFilledTemplate filledTemplate, PatternParameterAxis axis) {
        switch (axis) {
            case X:
                return (y, z) -> filledTemplate.setLineX(0, filledTemplate.getMax().getX(), y, z, true);
            case Y:
                return (x, z) -> filledTemplate.setLineY(x, 0, filledTemplate.getMax().getY(), z, true);
            case Z:
                return (x, y) -> filledTemplate.setLineZ(x, y, 0, filledTemplate.getMax().getZ(), true);
            default:
                throw new IllegalArgumentException("Unknown axis " + axis);
        }
    }

    private static PositionGetter getFillGetter(IFilledTemplate filledTemplate, PatternParameterAxis axis) {
        switch (axis) {
            case X:
                return (a, b) -> filledTemplate.get(0, a, b);
            case Y:
                return (a, b) -> filledTemplate.get(a, 0, b);
            case Z:
                return (a, b) -> filledTemplate.get(a, b, 0);
            default:
                throw new IllegalArgumentException("Unknown axis " + axis);
        }
    }

    protected abstract void genShape(int maxA, int maxB, LineList list);

    static class Point {
        final int a, b;

        Point(int a, int b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + this.a;
            result = prime * result + this.b;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) {
                return false;
            }
            Point other = (Point) obj;
            if (this.a != other.a) return false;
            if (this.b != other.b) return false;
            return true;
        }
    }

    public static class LineList {
        private PathIterator2d iterator;
        private int lastA, lastB;
        private int fillInA = -1, fillInB = -1;

        public LineList(PathIterator2d iterator) {
            this.iterator = iterator;
        }

        public void setFillPoint(int a, int b) {
            fillInA = a;
            fillInB = b;
        }

        public void moveTo(int a, int b) {
            this.lastA = a;
            this.lastB = b;
        }

        public void lineTo(int a, int b) {
            PositionUtil.forAllOnPath2d(lastA, lastB, a, b, iterator);
            moveTo(a, b);
        }

        public void lineFrom(int a, int b) {
            int a2 = lastA;
            int b2 = lastB;
            moveTo(a, b);
            lineTo(a2, b2);
            moveTo(a, b);
        }

        public void arc(int ca, int cb, double ra, double rb) {
            arc(ca, cb, ra, rb, 0, 0, ArcType.ARC);
        }

        public void arc(int ca, int cb, double ra, double rb, int da, int db, ArcType type) {
            if (ra <= 0) {
                throw new IllegalArgumentException("'ra' was less than or equal to 0! (Was " + ra + ")");
            }
            if (rb <= 0) {
                throw new IllegalArgumentException("'rb' was less than or equal to 0! (Was " + rb + ")");
            }
            double ra2 = ra * ra;
            double rb2 = rb * rb;

            // TODO: Fix (or replace) this algorithm with one that doesn't miss ends

            double sigma = 2 * rb2 + ra2 * (1 - 2 * rb);
            for (int a = 0, b = (int) rb; rb2 * a <= ra2 * b; a++) {
                iterator.iterate(ca - a, cb - b);
                if (type.shouldDrawSecondQuadrant()) {
                    iterator.iterate(ca + a + da, cb - b);
                    if (type.shouldDrawAllQuadrants()) {
                        iterator.iterate(ca - a, cb + b + db);
                        iterator.iterate(ca + a + da, cb + b + db);
                    }
                }
                if (sigma >= 0) {
                    sigma += 4 * ra2 * (1 - b);
                    b--;
                }
                sigma += rb2 * ((4 * a) + 6);
            }

            sigma = 2 * ra2 + rb2 * (1 - 2 * ra);
            for (int a = (int) ra, b = 0; ra2 * b <= rb2 * a; b++) {
                iterator.iterate(ca - a, cb - b);
                if (type.shouldDrawSecondQuadrant()) {
                    iterator.iterate(ca + a + da, cb - b);
                    if (type.shouldDrawAllQuadrants()) {
                        iterator.iterate(ca - a, cb + b + db);
                        iterator.iterate(ca + a + da, cb + b + db);
                    }
                }
                if (sigma >= 0) {
                    sigma += 4 * rb2 * (1 - a);
                    a--;
                }
                sigma += ra2 * ((4 * b) + 6);
            }
        }
    }

    public enum ArcType {
        ARC(false, false),
        SEMI_CIRCLE(true, false),
        FULL_CIRCLE(true, true);

        final boolean second, all;

        private ArcType(boolean second, boolean all) {
            this.second = second;
            this.all = all;
        }

        public boolean shouldDrawSecondQuadrant() {
            return second;
        }

        public boolean shouldDrawAllQuadrants() {
            return all;
        }
    }
}
