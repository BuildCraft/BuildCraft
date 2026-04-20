/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.core.statements;

import java.util.Locale;

import ct.buildcraft.api.statements.IActionExternal;
import ct.buildcraft.api.statements.IStatement;
import ct.buildcraft.api.statements.IStatementContainer;
import ct.buildcraft.api.statements.IStatementParameter;
import ct.buildcraft.api.tiles.IControllable;
import ct.buildcraft.api.tiles.IControllable.Mode;
import ct.buildcraft.api.tiles.TilesAPI;
import ct.buildcraft.core.BCCoreSprites;
import ct.buildcraft.core.BCCoreStatements;
import ct.buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
    public Component getDescription() {
        return Component.translatable("gate.action.machine." + mode.name().toLowerCase(Locale.ROOT));
    }

    @Override
    public void actionActivate(BlockEntity target, Direction side, IStatementContainer source, IStatementParameter[] parameters) {
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
