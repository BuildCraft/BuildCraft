/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.lib.statement;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.render.ISprite;
import buildcraft.api.statements.*;
import buildcraft.lib.misc.ColourUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public abstract class StatementWrapper implements IStatement, Comparable<StatementWrapper> {
    public final IStatement delegate;

    /** Used to determine the background colour of triggers and actions. */
    public final EnumPipePart sourcePart;

    public StatementWrapper(IStatement delegate, EnumPipePart sourcePart) {
        this.delegate = delegate;
        this.sourcePart = sourcePart;
    }

    /** @see buildcraft.api.statements.IStatement#getUniqueTag() */
    @Override
    public String getUniqueTag() {
        return this.delegate.getUniqueTag();
    }

    /** @see buildcraft.api.statements.IStatement#maxParameters() */
    @Override
    public int maxParameters() {
        return this.delegate.maxParameters();
    }

    /** @see buildcraft.api.statements.IStatement#minParameters() */
    @Override
    public int minParameters() {
        return this.delegate.minParameters();
    }

    /** @see buildcraft.api.statements.IStatement#getDescription() */
    @Override
    public ITextComponent getDescription() {
        return this.delegate.getDescription();
    }

    // Calen

    /** @see IStatement#getDescriptionKey() */
    @Override
    public String getDescriptionKey() {
        return this.delegate.getDescriptionKey();
    }

    /** @see buildcraft.api.statements.IStatement#createParameter(int) */
    @Override
    public IStatementParameter createParameter(int index) {
        return this.delegate.createParameter(index);
    }

    /** @see buildcraft.api.statements.IStatement#rotateLeft() */
    @Override
    public IStatement rotateLeft() {
        return this.delegate.rotateLeft();
    }

    /** @see buildcraft.api.statements.IStatement#getSprite() */
    @Override
    public ISprite getSprite() {
        return this.delegate.getSprite();
    }

    public TileEntity getNeighbourTile(IStatementContainer source) {
        return source.getNeighbourTile(sourcePart.face);
    }

    @Override
    public abstract StatementWrapper[] getPossible();

    @Override
    public boolean isPossibleOrdered() {
        return delegate.isPossibleOrdered();
    }

    @Override
    public List<ITextComponent> getTooltip() {
        List<ITextComponent> list = delegate.getTooltip();
        if (sourcePart != EnumPipePart.CENTER) {
            list = new ArrayList<>(list);
            ITextComponent translated = new StringTextComponent(ColourUtil.getTextFullTooltip(sourcePart.face));
//            list.add(new StringTextComponent(LocaleUtil.localize("gate.side", translated)));
            list.add(new TranslationTextComponent("gate.side", translated));
        }
        return list;
    }

    @Override
    public List<String> getTooltipKey() {
        List<String> list = delegate.getTooltipKey();
        if (sourcePart != EnumPipePart.CENTER) {
            list = new ArrayList<>(list);
            list.add("gate.side." + ColourUtil.getTextFullTooltip(sourcePart.face));
        }
        return list;
    }

    @Override
    public <T> T convertTo(Class<T> clazz) {
        T t = delegate.convertTo(clazz);
        if (t != null) {
            return t;
        }
        // As we need to keep the wrapper it's not quite as simple as "return t;"
        if (clazz.isAssignableFrom(TriggerWrapper.class)) {

            ITrigger trigger = delegate.convertTo(ITrigger.class);
            if (trigger != null) {
                return clazz.cast(TriggerWrapper.wrap(trigger, sourcePart.face));
            }
        } else if (clazz.isAssignableFrom(ActionWrapper.class)) {

            IAction action = delegate.convertTo(IAction.class);
            if (action != null) {
                return clazz.cast(ActionWrapper.wrap(action, sourcePart.face));
            }
        }
        return null;
    }

    @Override
    public int compareTo(StatementWrapper o) {
        if (sourcePart != o.sourcePart) {
            return Integer.compare(o.sourcePart.getIndex(), sourcePart.getIndex());
        }
        if (delegate == o.delegate) {
            return 0;
        }
        if (delegate.getClass() == o.delegate.getClass()) {
            IStatement[] poss = delegate.getPossible();
            IStatement[] oPoss = o.delegate.getPossible();
            if (Arrays.equals(poss, oPoss)) {
                int idxThis = -1;
                int idxThat = -1;
                for (int i = 0; i < poss.length; i++) {
                    if (poss[i] == delegate) {
                        idxThis = i;
                    }
                    if (poss[i] == o.delegate) {
                        idxThat = i;
                    }
                }
                if (idxThis != idxThat && idxThis != -1 && idxThat != -1) {
                    return Integer.compare(idxThis, idxThat);
                }
            }
        }
        return getUniqueTag().compareTo(o.getUniqueTag());
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourcePart, getUniqueTag());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != getClass()) {
            return false;
        }
        StatementWrapper other = (StatementWrapper) obj;
        return sourcePart == other.sourcePart && getUniqueTag().equals(other.getUniqueTag());
    }
}
