/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.builders.patterns;

import java.util.Map;
import java.util.TreeMap;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.blueprints.SchematicMask;
import buildcraft.api.enums.EnumFillerPattern;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.core.Box;
import buildcraft.core.blueprints.Blueprint;
import buildcraft.core.blueprints.BlueprintBase;
import buildcraft.core.blueprints.BptBuilderTemplate;
import buildcraft.core.blueprints.SchematicRegistry;
import buildcraft.core.blueprints.Template;
import buildcraft.core.lib.utils.StringUtils;

public abstract class FillerPattern implements IFillerPattern {

    public static final Map<String, FillerPattern> patterns = new TreeMap<String, FillerPattern>();
    public final EnumFillerPattern type;
    private final String tag;
    private final ResourceLocation location;

    @SideOnly(Side.CLIENT)
    private TextureAtlasSprite sprite;

    public FillerPattern(String tag, EnumFillerPattern type) {
        this.tag = tag;
        this.type = type;
        patterns.put(getUniqueTag(), this);
        location = new ResourceLocation("buildcraftcore:items/fillerPatterns/" + tag);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public String getDescription() {
        return StringUtils.localize("fillerpattern." + tag);
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
                        template.contents[x][y][z] = new SchematicMask(true);
                    }
                }
            }
        }
    }

    /** Generates an empty in a given area */
    public static void empty(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, Template template) {
        int lastX = Integer.MAX_VALUE, lastY = Integer.MAX_VALUE, lastZ = Integer.MAX_VALUE;

        for (int y = yMax; y >= yMin; y--) {
            for (int x = xMin; x <= xMax; ++x) {
                for (int z = zMin; z <= zMax; ++z) {
                    if (isValid(x, y, z, template)) {
                        template.contents[x][y][z] = null;
                    }
                }
            }
        }
    }

    /** Generates a flatten in a given area */
    public static void flatten(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, Template template) {
        int lastX = Integer.MAX_VALUE, lastY = Integer.MAX_VALUE, lastZ = Integer.MAX_VALUE;

        for (int x = xMin; x <= xMax; ++x) {
            for (int z = zMin; z <= zMax; ++z) {
                for (int y = yMax; y >= yMin; --y) {
                    if (isValid(x, y, z, template)) {
                        template.contents[x][y][z] = new SchematicMask(true);
                    }
                }
            }
        }
    }

    public abstract Template getTemplate(Box box, World world, IStatementParameter[] parameters);

    public Blueprint getBlueprint(Box box, World world, IStatementParameter[] parameters, IBlockState state) {
        Blueprint result = new Blueprint(box.sizeX(), box.sizeY(), box.sizeZ());

        try {
            Template tmpl = getTemplate(box, world, parameters);

            for (int x = 0; x < box.sizeX(); ++x) {
                for (int y = 0; y < box.sizeY(); ++y) {
                    for (int z = 0; z < box.sizeZ(); ++z) {
                        if (tmpl.contents[x][y][z] != null) {
                            result.contents[x][y][z] = SchematicRegistry.INSTANCE.createSchematicBlock(state);
                        }

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return result;
    }

    public BptBuilderTemplate getTemplateBuilder(Box box, World world, IStatementParameter[] parameters) {
        return new BptBuilderTemplate(getTemplate(box, world, parameters), world, new BlockPos(box.xMin, box.yMin, box.zMin));
    }

    private static boolean isValid(int x, int y, int z, BlueprintBase bpt) {
        return x >= 0 && y >= 0 && z >= 0 && x < bpt.sizeX && y < bpt.sizeY && z < bpt.sizeZ;
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void stitchTextures(TextureStitchEvent.Pre event) {
        sprite = event.map.registerSprite(location);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getGuiSprite() {
        return sprite;
    }
}
