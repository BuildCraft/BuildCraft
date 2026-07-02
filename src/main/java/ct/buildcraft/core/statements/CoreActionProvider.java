/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.core.statements;

import java.util.Collection;

import javax.annotation.Nonnull;

import ct.buildcraft.api.statements.IActionExternal;
import ct.buildcraft.api.statements.IActionInternal;
import ct.buildcraft.api.statements.IActionInternalSided;
import ct.buildcraft.api.statements.IActionProvider;
import ct.buildcraft.api.statements.IStatementContainer;
import ct.buildcraft.api.statements.containers.IRedstoneStatementContainer;
import ct.buildcraft.api.tiles.IControllable;
import ct.buildcraft.api.tiles.TilesAPI;
import ct.buildcraft.core.BCCoreStatements;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

public enum CoreActionProvider implements IActionProvider {
    INSTANCE;

    @Override
    public void addInternalActions(Collection<IActionInternal> res, IStatementContainer container) {
        if (container instanceof IRedstoneStatementContainer) {
            res.add(BCCoreStatements.ACTION_REDSTONE);
        }
    }

    @Override
    public void addInternalSidedActions(Collection<IActionInternalSided> actions, IStatementContainer container, @Nonnull Direction side) { }

    @Override
    public void addExternalActions(Collection<IActionExternal> res, @Nonnull Direction side, BlockEntity tile) {
        IControllable controllable = tile.getCapability(TilesAPI.CAP_CONTROLLABLE, side.getOpposite()).orElse(null);
        if (controllable != null) {
            for (ActionMachineControl action : BCCoreStatements.ACTION_MACHINE_CONTROL) {
                if (controllable.acceptsControlMode(action.mode)) {
                    res.add(action);
                }
            }
        }
    }
}
