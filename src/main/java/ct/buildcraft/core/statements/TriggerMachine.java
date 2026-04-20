/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.core.statements;

import ct.buildcraft.api.statements.IStatement;
import ct.buildcraft.api.statements.IStatementContainer;
import ct.buildcraft.api.statements.IStatementParameter;
import ct.buildcraft.api.statements.ITriggerExternal;
import ct.buildcraft.api.tiles.IHasWork;
import ct.buildcraft.api.tiles.TilesAPI;
import ct.buildcraft.core.BCCoreSprites;
import ct.buildcraft.core.BCCoreStatements;
import ct.buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TriggerMachine extends BCStatement implements ITriggerExternal {

    public final boolean active;

    public TriggerMachine(boolean active) {
        super("buildcraft:work." + (active ? "scheduled" : "done"), "buildcraft.work." + (active ? "scheduled" : "done"));
        this.active = active;
    }

    @Override
    public Component getDescription() {
        return Component.translatable("gate.trigger.machine." + (active ? "scheduled" : "done"));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public SpriteHolder getSprite() {
        return active ? BCCoreSprites.TRIGGER_MACHINE_ACTIVE : BCCoreSprites.TRIGGER_MACHINE_INACTIVE;
    }

    @Override
    public boolean isTriggerActive(BlockEntity tile, Direction side, IStatementContainer container, IStatementParameter[] parameters) {
        IHasWork hasWork = tile.getCapability(TilesAPI.CAP_HAS_WORK, side.getOpposite()).orElse(null);
        if (hasWork == null) {
            return false;
        }
        return hasWork.hasWork() == active;

    }

    @Override
    public IStatement[] getPossible() {
        return new IStatement[] { BCCoreStatements.TRIGGER_MACHINE_ACTIVE, BCCoreStatements.TRIGGER_MACHINE_INACTIVE };
    }
}
