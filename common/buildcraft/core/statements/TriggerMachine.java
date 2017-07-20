/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.statements;

import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerExternal;
import buildcraft.api.tiles.TilesAPI;
import buildcraft.core.BCCoreSprites;
import buildcraft.core.BCCoreStatements;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import buildcraft.lib.misc.LocaleUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TriggerMachine extends BCStatement implements ITriggerExternal {

    boolean active;

    public TriggerMachine(boolean active) {
        super("buildcraft:work." + (active ? "scheduled" : "done"), "buildcraft.work." + (active ? "scheduled" : "done"));
        this.active = active;
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.trigger.machine." + (active ? "scheduled" : "done"));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SpriteHolder getSpriteHolder() {
        return active ? BCCoreSprites.TRIGGER_MACHINE_ACTIVE : BCCoreSprites.TRIGGER_MACHINE_INACTIVE;
    }

    @Override
    public boolean isTriggerActive(TileEntity tile, EnumFacing side, IStatementContainer container, IStatementParameter[] parameters) {
        return tile.hasCapability(TilesAPI.CAP_HAS_WORK, null) &&
                active == tile.getCapability(TilesAPI.CAP_HAS_WORK, null).hasWork();

    }

    @Override
    public IStatement[] getPossible() {
        return new IStatement[] { BCCoreStatements.TRIGGER_MACHINE_ACTIVE, BCCoreStatements.TRIGGER_MACHINE_INACTIVE };
    }
}
