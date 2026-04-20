/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.core.statements;

import ct.buildcraft.api.statements.IActionInternal;
import ct.buildcraft.api.statements.IStatementContainer;
import ct.buildcraft.api.statements.IStatementParameter;
import ct.buildcraft.api.statements.containers.IRedstoneStatementContainer;
import ct.buildcraft.api.statements.containers.ISidedStatementContainer;
import ct.buildcraft.core.BCCoreSprites;
import ct.buildcraft.core.BCCoreStatements;
import ct.buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ActionRedstoneOutput extends BCStatement implements IActionInternal {

    protected ActionRedstoneOutput(String s) {
        // Used by fader output
        super(s);
    }

    public ActionRedstoneOutput() {
        super("buildcraft:redstone.output", "buildcraft.redstone.output");
    }

    @Override
    public Component getDescription() {
        return Component.translatable("gate.action.redstone.signal");
    }

    @Override
    public IStatementParameter createParameter(int index) {
        switch (index) {
            case 0:
                return StatementParamGateSideOnly.ANY;
            default:
                return null;
        }
    }

    @Override
    public int maxParameters() {
        return 1;
    }

    protected boolean isSideOnly(IStatementParameter[] parameters) {
        if (parameters != null && parameters.length >= (getRGSOSlot() + 1)
            && parameters[getRGSOSlot()] instanceof StatementParamGateSideOnly) {
            return ((StatementParamGateSideOnly) parameters[getRGSOSlot()]).isSpecific;
        }

        return false;
    }

    @Override
    public void actionActivate(IStatementContainer source, IStatementParameter[] parameters) {
        if (source instanceof IRedstoneStatementContainer) {
            Direction side = null;
            if (source instanceof ISidedStatementContainer && isSideOnly(parameters)) {
                side = ((ISidedStatementContainer) source).getSide();
            }
            ((IRedstoneStatementContainer) source).setRedstoneOutput(side, getSignalLevel(parameters));
        }
    }

    protected int getRGSOSlot() {
        return 0;
    }

    protected int getSignalLevel(IStatementParameter[] parameters) {
        return 15;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public SpriteHolder getSprite() {
        return BCCoreSprites.ACTION_REDSTONE;
    }

    @Override
    public <T> T convertTo(Class<T> clazz) {
        T obj = super.convertTo(clazz);
        if (obj != null) {
            return obj;
        }
        if (clazz.isInstance(BCCoreStatements.TRIGGER_REDSTONE_ACTIVE)) {
            return clazz.cast(BCCoreStatements.TRIGGER_REDSTONE_ACTIVE);
        }
        return null;
    }
}
