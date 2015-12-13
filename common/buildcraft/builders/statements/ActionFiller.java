/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.statements;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import buildcraft.api.filler.FillerManager;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.api.statements.IActionExternal;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.builders.TileFiller;
import buildcraft.core.builders.patterns.FillerPattern;
import buildcraft.core.statements.BCStatement;

public class ActionFiller extends BCStatement implements IActionExternal {

    private static final Map<FillerPattern, ActionFiller> actions = Maps.newHashMap();

    public static void resetMap() {
        actions.clear();
        for (IFillerPattern pattern : FillerManager.registry.getPatterns()) {
            FillerPattern fil = (FillerPattern) pattern;
            actions.put(fil, new ActionFiller(fil));
        }
    }

    public static ActionFiller getForPattern(FillerPattern pattern) {
        return actions.get(pattern);
    }

    public final FillerPattern pattern;

    private ActionFiller(FillerPattern pattern) {
        super("filler:" + pattern.getUniqueTag());
        this.pattern = pattern;
        setBuildCraftLocation("core", "filler/patterns/" + pattern.type.getName());
    }

    @Override
    public String getDescription() {
        return "Pattern: " + pattern.getDescription();
    }

    @Override
    public TextureAtlasSprite getGuiSprite() {
        return pattern.getGuiSprite();
    }

    @Override
    public int minParameters() {
        return pattern.minParameters();
    }

    @Override
    public int maxParameters() {
        return pattern.maxParameters();
    }

    @Override
    public IStatementParameter createParameter(int index) {
        return pattern.createParameter(index);
    }

    @Override
    public void actionActivate(TileEntity target, EnumFacing side, IStatementContainer source, IStatementParameter[] parameters) {
        if (target instanceof TileFiller) {
            ((TileFiller) target).setPattern(pattern);
            ((TileFiller) target).patternParameters = parameters;
        }
    }
}
