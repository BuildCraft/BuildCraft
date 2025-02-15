/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.gui;

import buildcraft.api.core.render.ISprite;
import buildcraft.lib.BCLibSprites;
import buildcraft.lib.gui.BuildCraftGui;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.config.GuiConfigManager;
import buildcraft.lib.gui.ledger.Ledger_Neptune;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.silicon.tile.TileAssemblyTable;
import buildcraft.silicon.tile.TileLaserTableBase;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class LedgerTablePower extends Ledger_Neptune {
    private static final int OVERLAY_COLOUR = 0xFF_D4_6C_1F;// 0xFF_FF_55_11;// TEMP!
    private static final int SUB_HEADER_COLOUR = 0xFF_AA_AF_b8;
    private static final int TEXT_COLOUR = 0xFF_00_00_00;

    public final TileLaserTableBase tile;

    public LedgerTablePower(BuildCraftGui gui, TileAssemblyTable tile, boolean expandPositive) {
        super(gui, OVERLAY_COLOUR, expandPositive);
        this.tile = tile;
        title = "gui.power";

        appendText(LocaleUtil.localize("gui.assemblyCurrentRequired") + ":", SUB_HEADER_COLOUR).setDropShadow(true);
        appendText(() -> LocaleUtil.localizeMj(tile.getTarget()), TEXT_COLOUR);
        appendText(LocaleUtil.localize("gui.stored") + ":", SUB_HEADER_COLOUR).setDropShadow(true);
        appendText(() -> LocaleUtil.localizeMj(tile.power), TEXT_COLOUR);
        appendText(LocaleUtil.localize("gui.assemblyRate") + ":", SUB_HEADER_COLOUR).setDropShadow(true);
        appendText(() -> LocaleUtil.localizeMjFlow(tile.avgPowerClient), TEXT_COLOUR);
        calculateMaxSize();

        setOpenProperty(GuiConfigManager.getOrAddBoolean(new ResourceLocation("buildcraftsilicon:all_tables"), "ledger.power.is_open", false));
    }

    @Override
    protected void drawIcon(GuiGraphics guiGraphics, double x, double y) {
        ISprite sprite = tile.avgPowerClient > 0 ? BCLibSprites.ENGINE_ACTIVE : BCLibSprites.ENGINE_INACTIVE;
        GuiIcon.draw(sprite, guiGraphics, x, y, x + 16, y + 16);
    }
}
