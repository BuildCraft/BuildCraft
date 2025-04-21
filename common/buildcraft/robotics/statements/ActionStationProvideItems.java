/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.statements;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.statements.*;
import buildcraft.core.statements.BCStatement;
import buildcraft.lib.inventory.filter.StatementParameterStackFilter;
import buildcraft.robotics.BCRoboticsSprites;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ActionStationProvideItems extends BCStatement implements IActionInternal {

    public ActionStationProvideItems() {
        super("buildcraft:station.provide_items");
    }

    @Override
    public ITextComponent getDescription() {
        return new TranslationTextComponent("gate.action.station.provide_items");
    }

    @Override
    public String getDescriptionKey() {
        return "gate.action.station.provide_items";
    }

    @Nullable
    @Override
    public ISprite getSprite() {
        return BCRoboticsSprites.ACTION_STATION_PROVIDE_ITEMS;
    }

    @Override
    public int maxParameters() {
        return 3;
    }

    @Override
    public IStatementParameter createParameter(int index) {
        return new StatementParameterItemStack();
    }

    @Override
    public void actionActivate(IStatementContainer source, IStatementParameter[] parameters) {

    }

    public static boolean canExtractItem(DockingStation station, @Nonnull ItemStack stack) {
        boolean hasFilter = false;

        for (StatementSlot s : station.getActiveActions()) {
            if (s.statement instanceof ActionStationProvideItems) {
                StatementParameterStackFilter param = new StatementParameterStackFilter(s.parameters);

                if (param.hasFilter()) {
                    hasFilter = true;

                    if (param.matches(stack)) {
                        return true;
                    }
                }
            }
        }

        return !hasFilter;
    }
}
