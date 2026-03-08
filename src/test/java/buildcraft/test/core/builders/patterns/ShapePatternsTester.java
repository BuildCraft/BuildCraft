package buildcraft.test.core.builders.patterns;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Direction;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import buildcraft.api.filler.FillerManager;
import buildcraft.api.filler.IFilledTemplate;
import buildcraft.api.filler.IFillerPatternShape;
import buildcraft.api.statements.IStatementParameter;

import buildcraft.lib.misc.StringUtilBC;
import buildcraft.lib.misc.VecUtil;

import buildcraft.builders.BCBuildersStatements;
import buildcraft.builders.registry.FillerRegistry;
import buildcraft.builders.snapshot.Snapshot;
import buildcraft.builders.snapshot.Template;
import buildcraft.builders.snapshot.pattern.parameter.PatternParameterFacing;
import buildcraft.builders.snapshot.pattern.parameter.PatternParameterHollow;
import buildcraft.test.VanillaSetupBaseTester;

@RunWith(Theories.class)
public class ShapePatternsTester extends VanillaSetupBaseTester {
    @DataPoints
    public static List<IFillerPatternShape> patterns;

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
        patterns = Arrays.stream(BCBuildersStatements.PATTERNS)
            .filter(IFillerPatternShape.class::isInstance)
            .map(IFillerPatternShape.class::cast)
            .collect(Collectors.toList());
    }

    @Theory
    public void testTinyTemplate(IFillerPatternShape pattern, BlockPos size) {
        System.out.print("Testing pattern " + pattern.getUniqueTag() + " in " + StringUtilBC.blockPosToString(size));

        try {
            IStatementParameter[] params = new IStatementParameter[pattern.maxParameters()];
            for (int i = 0; i < params.length; i++) {
                params[i] = pattern.createParameter(i);
            }

            IFilledTemplate filledTemplate = createFilledTemplate(size);
            boolean b = pattern.fillTemplate(filledTemplate, params);
            if (pattern == BCBuildersStatements.PATTERN_NONE) {
                Assert.assertFalse(b);
            } else {
                Assert.assertTrue(b);
            }
            System.out.println(" -> success");
        } catch (Throwable t) {
            System.out.println(" -> fail");
            throw t;
        }
    }

    private IFilledTemplate createFilledTemplate(BlockPos size) {
        Template template = new Template();
        template.size = size;
        template.offset = BlockPos.ZERO;
        template.data = new BitSet(Snapshot.getDataSize(size));
        return template.getFilledTemplate();
    }

    /** Ensure that (for the same implicit size sphere) SPHERE, SPHERE_HALF, SPHERE_QUARTER, and SPHERE_EIGHTH all
     * generate the same sphere.
     * 
     * @param size an eighth of the size of the entire sphere. */
    @Theory
    public void testSphereEquality(BlockPos size) {
        BlockPos fullSize = new BlockPos(size.getX() * 2, size.getY() * 2, size.getZ() * 2);

        System.out.println("Testing spheres for equality in " + StringUtilBC.blockPosToString(fullSize));

        IStatementParameter[] fullParams = new IStatementParameter[] { //
            PatternParameterHollow.HOLLOW, //
        };
        IFilledTemplate filledTemplateFull = createFilledTemplate(fullSize);
        Assert.assertTrue(BCBuildersStatements.PATTERN_SPHERE.fillTemplate(filledTemplateFull, fullParams));
        System.out.println("Full:\n" + filledTemplateFull);

        // Test halfs
        for (Direction face : Direction.values()) {
            BlockPos halfSize = VecUtil.replaceValue(fullSize, face.getAxis(), VecUtil.getValue(size, face.getAxis()));
            IStatementParameter[] params = new IStatementParameter[] { //
                PatternParameterHollow.HOLLOW, //
                PatternParameterFacing.get(face) //
            };
            IFilledTemplate filledTemplateHalf = createFilledTemplate(halfSize);
            Assert.assertTrue(BCBuildersStatements.PATTERN_HEMI_SPHERE.fillTemplate(filledTemplateHalf, params));
            System.out.println("Half:\n" + filledTemplateHalf);
            int dx = face == Direction.WEST ? filledTemplateHalf.getSize().getX() : 0;
            int dy = face == Direction.DOWN ? filledTemplateHalf.getSize().getY() : 0;
            int dz = face == Direction.NORTH ? filledTemplateHalf.getSize().getZ() : 0;
            for (int z = 0; z <= filledTemplateHalf.getMax().getZ(); z++) {
                for (int y = 0; y <= filledTemplateHalf.getMax().getY(); y++) {
                    for (int x = 0; x <= filledTemplateHalf.getMax().getX(); x++) {
                        if (filledTemplateFull.get(x + dx, y + dy, z + dz) != filledTemplateHalf.get(x, y, z)) {
                            Assert.fail(
                                String.format(
                                    "Half sphere[%s] didn't match full sphere at (%s, %s, %s)",
                                    face,
                                    x,
                                    y,
                                    z
                                )
                            );
                        }
                    }
                }
            }
        }

        // TODO: Test quarters
    }
}
