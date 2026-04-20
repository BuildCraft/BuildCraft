/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.silicon.gui;

import com.mojang.blaze3d.vertex.PoseStack;

import ct.buildcraft.api.core.render.ISprite;
import ct.buildcraft.lib.BCLibSprites;
import ct.buildcraft.lib.gui.BuildCraftGui;
import ct.buildcraft.lib.gui.GuiIcon;
import ct.buildcraft.lib.gui.config.GuiConfigManager;
import ct.buildcraft.lib.gui.ledger.Ledger_Neptune;
import ct.buildcraft.lib.misc.LocaleUtil;
import ct.buildcraft.silicon.tile.TileAssemblyTable;
import ct.buildcraft.silicon.tile.TileLaserTableBase;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class LedgerTablePower extends Ledger_Neptune {//TODO
    private static final int OVERLAY_COLOUR = 0xFF_D4_6C_1F;// 0xFF_FF_55_11;// TEMP!
    private static final int SUB_HEADER_COLOUR = 0xFF_AA_AF_b8;
    private static final int TEXT_COLOUR = 0xFF_00_00_00;

    public final TileLaserTableBase tile;

    public LedgerTablePower(BuildCraftGui gui, TileAssemblyTable tile, boolean expandPositive) {
        super(gui, OVERLAY_COLOUR, expandPositive);
        this.tile = tile;
        title = Component.translatable("gui.power");

        appendText(Component.literal(LocaleUtil.localize("gui.assemblyCurrentRequired") + ":"), SUB_HEADER_COLOUR).setDropShadow(true);
        appendText(() -> LocaleUtil.localizeMj(tile.getTarget()), TEXT_COLOUR);
        appendText(Component.literal(LocaleUtil.localize("gui.stored") + ":"), SUB_HEADER_COLOUR).setDropShadow(true);
        appendText(() -> LocaleUtil.localizeMj(tile.power), TEXT_COLOUR);
        appendText(Component.literal(LocaleUtil.localize("gui.assemblyRate") + ":"), SUB_HEADER_COLOUR).setDropShadow(true);
        appendText(() -> LocaleUtil.localizeMjFlow(tile.avgPowerClient), TEXT_COLOUR);
        calculateMaxSize();

        setOpenProperty(GuiConfigManager.getOrAddBoolean(new ResourceLocation("buildcraftsilicon:all_tables"), "ledger.power.is_open", false));
    }

    @Override
    protected void drawIcon(PoseStack pose, double x, double y) {
        ISprite sprite = tile.avgPowerClient > 0 ? BCLibSprites.ENGINE_ACTIVE : BCLibSprites.ENGINE_INACTIVE;
        GuiIcon.draw(pose, sprite, x, y, x + 16, y + 16);
    }
}
