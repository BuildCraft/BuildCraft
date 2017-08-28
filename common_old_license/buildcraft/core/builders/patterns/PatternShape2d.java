package buildcraft.core.builders.patterns;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;

import buildcraft.api.core.IBox;
import buildcraft.api.filler.FilledTemplate;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.containers.IFillerStatementContainer;

import buildcraft.lib.misc.PositionUtil;
import buildcraft.lib.misc.PositionUtil.PathIterator2d;

public abstract class PatternShape2d extends Pattern {

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
                return PatternParameterXZDir.NORTH;
        }
        return null;
    }

    @Override
    public FilledTemplate createTemplate(IFillerStatementContainer filler, IStatementParameter[] params) {
        IBox box = filler.getBox();
        FilledTemplate template = new FilledTemplate(box);

        PatternParameterAxis axis = getParam(0, params, PatternParameterAxis.Y);
        PatternParameterXZDir dir = getParam(2, params, PatternParameterXZDir.NORTH);

        PathIterator2d iterator = getIterator(template, axis);

        int maxA = axis == PatternParameterAxis.X ? template.maxY : template.maxX;
        int maxB = axis == PatternParameterAxis.Z ? template.maxY : template.maxZ;

        if (dir.dir.getAxis() == Axis.X) {
            int maxT = maxA;
            maxA = maxB;
            maxB = maxT;
            final PathIterator2d old = iterator;
            iterator = (a, b) -> old.iterate(b, a);
        }
        if (dir.dir.getAxisDirection() == AxisDirection.POSITIVE) {
            final PathIterator2d old = iterator;
            final int max_a = maxA;
            final int max_b = maxB;
            // Technically this is wrong... but it doesn't matter for any of
            // the shapes that use this, as they are all symetrical
            iterator = (a, b) -> old.iterate(max_a - a, max_b - b);
        }

        LineList list = new LineList(iterator);
        genShape(maxA, maxB, list);

        if (getParam(1, params, PatternParameterHollow.HOLLOW).filled) {
            int fillA = list.fillA;
            int fillB = list.fillB;
            if (fillA != -1 && fillB != -1) {
                PositionGetter getter = getFillGetter(template, axis);

                // Expand outwards from the point
                Set<Point> visited = new HashSet<>();
                List<Point> open = new ArrayList<>();
                open.add(new Point(fillA, fillB));
                while (!open.isEmpty()) {
                    List<Point> next = new ArrayList<>();
                    for (Point p : open) {
                        if (p.a < 0 || p.a >= template.sizeX) {
                            continue;
                        }
                        if (p.b < 0 || p.b >= template.sizeZ) {
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
            }
        }
        return template;
    }

    @FunctionalInterface
    interface PositionGetter {
        boolean isFilled(int a, int b);
    }

    private static PathIterator2d getIterator(FilledTemplate template, PatternParameterAxis axis) {
        switch (axis) {
            case X:
                return template::fillAxisX;
            case Y:
                return template::fillAxisY;
            case Z:
                return template::fillAxisZ;
            default:
                throw new IllegalArgumentException("Unknown axis " + axis);
        }
    }

    private static PositionGetter getFillGetter(FilledTemplate template, PatternParameterAxis axis) {
        switch (axis) {
            case X:
                return (a, b) -> template.get(0, a, b);
            case Y:
                return (a, b) -> template.get(a, 0, b);
            case Z:
                return (a, b) -> template.get(a, b, 0);
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
        private int fillA = -1, fillB = -1;

        public LineList(PathIterator2d iterator) {
            this.iterator = iterator;
        }

        public void setFillPoint(int a, int b) {
            fillA = a;
            fillB = b;
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
            ra = Math.max(1, ra);
            rb = Math.max(1, rb);
            double ra2 = ra * ra;
            double rb2 = rb * rb;

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
