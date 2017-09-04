package buildcraft.test.core.builders.patterns;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.core.IBox;
import buildcraft.api.filler.FilledTemplate;
import buildcraft.api.filler.FillerManager;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.containers.IFillerStatementContainer;

import buildcraft.lib.misc.StringUtilBC;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.misc.data.Box;
import buildcraft.lib.registry.FillerRegistry;

import buildcraft.core.BCCoreStatements;
import buildcraft.core.builders.patterns.Pattern;
import buildcraft.core.builders.patterns.PatternParameterFacing;
import buildcraft.core.builders.patterns.PatternParameterHollow;
import buildcraft.test.VanillaSetupBaseTester;

@RunWith(Theories.class)
public class DefaultPatternTester extends VanillaSetupBaseTester {

    @DataPoints
    public static Pattern[] patterns;

    /** Some randomly chosen sizes. Ideally we would test all possibilities, but that would take too long so here are 4.
     * (Hopefully enough to show up regressions). */
    @DataPoints
    public static BlockPos[] sizes = { //
        new BlockPos(1, 1, 1), new BlockPos(2, 1, 1), new BlockPos(3, 1, 1), //
        new BlockPos(2, 2, 2), new BlockPos(3, 2, 2), new BlockPos(4, 2, 2), //
        new BlockPos(2, 3, 2), new BlockPos(2, 2, 3), new BlockPos(2, 8, 2), //
        new BlockPos(3, 3, 3), new BlockPos(4, 4, 4), new BlockPos(5, 5, 5), //
        new BlockPos(6, 6, 6), new BlockPos(7, 7, 7), new BlockPos(11, 13, 12) //
    };

    @BeforeClass
    public static void setupRegistries() {
        FillerManager.registry = FillerRegistry.INSTANCE;
        patterns = BCCoreStatements.PATTERNS;
    }

    @Theory
    public void testTinyTemplate(Pattern pattern, BlockPos size) {
        // Horizon and flattern works a little differently, so don't test it here
        Assume.assumeFalse(pattern == BCCoreStatements.PATTERN_HORIZON);
        Assume.assumeFalse(pattern == BCCoreStatements.PATTERN_FLATTEN);
        System.out.print("Testing pattern " + pattern.getUniqueTag() + " in " + StringUtilBC.blockPosToString(size));

        try {
            IStatementParameter[] params = new IStatementParameter[pattern.maxParameters()];
            for (int i = 0; i < params.length; i++) {
                params[i] = pattern.createParameter(i);
            }

            Box box = new Box(BlockPos.ORIGIN, size.subtract(VecUtil.POS_ONE));
            TestFiller filler = new TestFiller(box);

            FilledTemplate tpl = pattern.createTemplate(filler, params);
            if (pattern == BCCoreStatements.PATTERN_NONE) {
                Assert.assertNull(tpl);
            } else {
                Assert.assertNotNull(tpl);
            }
            System.out.println(" -> success");
        } catch (Throwable t) {
            System.out.println(" -> fail");
            throw t;
        }
    }

    /** Ensure that (for the same implicit size sphere) SPHERE, SPHERE_HALF, SPHERE_QUARTER, and SPHERE_EIGHTH all
     * generate the same sphere.
     * 
     * @param size an eighth of the size of the entire sphere. */
    @Theory
    public void testSphereEquality(BlockPos size) {
        BlockPos fullSize = new BlockPos(size.getX() * 2, size.getY() * 2, size.getZ() * 2);

        System.out.println("Testing spheres for equality in " + StringUtilBC.blockPosToString(fullSize));

        TestFiller filler = new TestFiller(new Box(BlockPos.ORIGIN, fullSize.subtract(VecUtil.POS_ONE)));
        IStatementParameter[] fullParams = new IStatementParameter[] { //
            PatternParameterHollow.HOLLOW, //
        };
        FilledTemplate tplFull = BCCoreStatements.PATTERN_SPHERE.createTemplate(filler, fullParams);
        Assert.assertNotNull(tplFull);
        System.out.println(tplFull);

        // Test halfs
        for (EnumFacing face : EnumFacing.VALUES) {
            BlockPos halfSize = VecUtil.replaceValue(fullSize, face.getAxis(), VecUtil.getValue(size, face.getAxis()));
            filler = new TestFiller(new Box(BlockPos.ORIGIN, halfSize.subtract(VecUtil.POS_ONE)));
            IStatementParameter[] params = new IStatementParameter[] { //
                PatternParameterHollow.HOLLOW, //
                PatternParameterFacing.get(face) //
            };
            FilledTemplate tplHalf = BCCoreStatements.PATTERN_HEMI_SPHERE.createTemplate(filler, params);
            Assert.assertNotNull(tplHalf);
            int dx = face == EnumFacing.WEST ? tplHalf.sizeX : 0;
            int dy = face == EnumFacing.DOWN ? tplHalf.sizeY : 0;
            int dz = face == EnumFacing.NORTH ? tplHalf.sizeZ : 0;
            for (int x = 0; x <= tplHalf.maxX; x++) {
                for (int y = 0; y <= tplHalf.maxY; y++) {
                    for (int z = 0; z <= tplHalf.maxZ; z++) {
                        if (tplFull.get(x + dx, y + dy, z + dz) != tplHalf.get(x, y, z)) {
                            Assert.fail(String.format("Half sphere[%s] didn't match full sphere at (%s, %s, %s)", face,
                                x, y, z));
                        }
                    }
                }
            }
        }

        // Test quarters
    }

    static class TestFiller implements IFillerStatementContainer {
        public final Box box;

        public TestFiller(Box box) {
            this.box = box;
        }

        @Override
        public TileEntity getNeighbourTile(EnumFacing side) {
            return null;
        }

        @Override
        public TileEntity getTile() {
            return null;
        }

        @Override
        public World getFillerWorld() {
            throw new AbstractMethodError("Can't create a world now, sorry.");
        }

        @Override
        public boolean hasBox() {
            return true;
        }

        @Override
        public IBox getBox() throws IllegalStateException {
            return box;
        }

        @Override
        public void setPattern(IFillerPattern pattern, IStatementParameter[] params) {}
    }
}
