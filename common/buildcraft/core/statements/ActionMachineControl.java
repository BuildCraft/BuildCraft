/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.statements;

import buildcraft.api.statements.IActionExternal;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.tiles.IControllable;
import buildcraft.api.tiles.IControllable.Mode;
import buildcraft.api.tiles.TilesAPI;
import buildcraft.core.BCCoreSprites;
import buildcraft.core.BCCoreStatements;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Locale;

public class ActionMachineControl extends BCStatement implements IActionExternal {
    public final Mode mode;

    public ActionMachineControl(Mode mode) {
        super(
                "buildcraft:machine." + mode.name().toLowerCase(Locale.ROOT),
                "buildcraft.machine." + mode.name().toLowerCase(Locale.ROOT)
        );
        this.mode = mode;
    }

    @Override
    public ITextComponent getDescription() {
//        return LocaleUtil.localize("gate.action.machine." + mode.name().toLowerCase(Locale.ROOT));
        return new TranslationTextComponent("gate.action.machine." + mode.name().toLowerCase(Locale.ROOT));
    }

    @Override
    public String getDescriptionKey() {
        return "gate.action.machine." + mode.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public void actionActivate(TileEntity target, Direction side, IStatementContainer source, IStatementParameter[] parameters) {
        IControllable controllable = target.getCapability(TilesAPI.CAP_CONTROLLABLE, side.getOpposite()).orElse(null);
        if (controllable != null && controllable.acceptsControlMode(mode)) {
            controllable.setControlMode(mode);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public SpriteHolder getSprite() {
        return BCCoreSprites.ACTION_MACHINE_CONTROL.get(mode);
    }

    @Override
    public IStatement[] getPossible() {
        return BCCoreStatements.ACTION_MACHINE_CONTROL;
    }
}
