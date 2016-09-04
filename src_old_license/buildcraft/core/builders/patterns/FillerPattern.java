/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.builders.patterns;

import java.util.Map;
import java.util.TreeMap;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.blueprints.SchematicMask;
import buildcraft.api.enums.EnumFillerPattern;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.core.blueprints.Blueprint;
import buildcraft.core.blueprints.BlueprintBase;
import buildcraft.core.blueprints.BptBuilderTemplate;
import buildcraft.core.blueprints.SchematicRegistry;
import buildcraft.core.blueprints.Template;
import buildcraft.core.lib.utils.BCStringUtils;
import buildcraft.lib.misc.data.Box;

public abstract class FillerPattern implements IFillerPattern {

    public static final Map<String, FillerPattern> patterns = new TreeMap<>();
    public final EnumFillerPattern type;
    private final String tag;
    private final ResourceLocation location;

    @SideOnly(Side.CLIENT)
    private TextureAtlasSprite sprite;

    public FillerPattern(String tag, EnumFillerPattern type) {
        this.tag = tag;
        this.type = type;
        patterns.put(getUniqueTag(), this);
        location = new ResourceLocation("buildcraftcore:filler/patterns/" + tag);
    }

    @Override
    public String getDescription() {
        return BCStringUtils.localize("fillerpattern." + tag);
    }

    @Override
    public IStatementParameter createParameter(int index) {
        return null;
    }

    @Override
    public IStatement rotateLeft() {
        return this;
    }

    @Override
    public String getUniqueTag() {
        return "buildcraft:" + tag;
    }

    @Override
    public int maxParameters() {
        return 0;
    }

    @Override
    public int minParameters() {
        return 0;
    }

    @Override
    public String toString() {
        return "Pattern: " + getUniqueTag();
    }

    /** Generates a filling in a given area */
    public static void fill(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, Template template) {

        for (int y = yMin; y <= yMax; ++y) {
            for (int x = xMin; x <= xMax; ++x) {
                for (int z = zMin; z <= zMax; ++z) {
                    if (isValid(x, y, z, template)) {
                        template.set(new BlockPos(x, y, z), new SchematicMask(true));
                    }
                }
            }
        }
    }

    /** Generates an empty in a given area */
    public static void empty(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, Template template) {
        for (int y = yMax; y >= yMin; y--) {
            for (int x = xMin; x <= xMax; ++x) {
                for (int z = zMin; z <= zMax; ++z) {
                    if (isValid(x, y, z, template)) {
                        template.set(new BlockPos(x, y, z), null);
                    }
                }
            }
        }
    }

    /** Generates a flatten in a given area */
    public static void flatten(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, Template template) {
        for (int x = xMin; x <= xMax; ++x) {
            for (int z = zMin; z <= zMax; ++z) {
                for (int y = yMax; y >= yMin; --y) {
                    if (isValid(x, y, z, template)) {
                        template.set(new BlockPos(x, y, z), new SchematicMask(true));
                    }
                }
            }
        }
    }

    public abstract Template getTemplate(Box box, World world, IStatementParameter[] parameters);

    public Blueprint getBlueprint(Box box, World world, IStatementParameter[] parameters, IBlockState state) {
        Blueprint result = new Blueprint(box.size());

        try {
            Template tmpl = getTemplate(box, world, parameters);

            for (BlockPos pos : BlockPos.getAllInBox(BlockPos.ORIGIN, box.size())) {
                if (tmpl.get(pos) != null) {
                    result.set(pos, SchematicRegistry.INSTANCE.createSchematicBlock(state));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return result;
    }

    public BptBuilderTemplate getTemplateBuilder(Box box, World world, IStatementParameter[] parameters) {
        return new BptBuilderTemplate(getTemplate(box, world, parameters), world, box.min());
    }

    private static boolean isValid(int x, int y, int z, BlueprintBase bpt) {
        return x >= 0 && y >= 0 && z >= 0 && x < bpt.size.getX() && y < bpt.size.getY() && z < bpt.size.getZ();
    }

    @Override
    public void registerIcons(TextureMap map) {
        sprite = map.registerSprite(location);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getGuiSprite() {
        return sprite;
    }
}
