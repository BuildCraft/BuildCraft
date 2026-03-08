/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.statements;

import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.containers.IRedstoneStatementContainer;
import buildcraft.api.statements.containers.ISidedStatementContainer;
import buildcraft.core.BCCoreSprites;
import buildcraft.core.BCCoreStatements;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ActionRedstoneOutput extends BCStatement implements IActionInternal {

    protected ActionRedstoneOutput(String s) {
        // Used by fader output
        super(s);
    }

    public ActionRedstoneOutput() {
        super(
                "buildcraft:redstone.output",
                "buildcraft.redstone.output"
        );
    }

    @Override
    public ITextComponent getDescription() {
//        return LocaleUtil.localize("gate.action.redstone.signal");
        return new TranslationTextComponent("gate.action.redstone.signal");
    }

    @Override
    public String getDescriptionKey() {
        return "gate.action.redstone.signal";
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
                && parameters[getRGSOSlot()] instanceof StatementParamGateSideOnly)
        {
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
