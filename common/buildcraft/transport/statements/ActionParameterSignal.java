/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.statements;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.gates.IGate;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementMouseClick;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.misc.StackUtil;
import buildcraft.transport.BCTransportSprites;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class ActionParameterSignal implements IStatementParameter {

    public static final ActionParameterSignal EMPTY = new ActionParameterSignal(null);
    private static final Map<DyeColor, ActionParameterSignal> SIGNALS;

    static {
        SIGNALS = new EnumMap<>(DyeColor.class);
        for (DyeColor colour : ColourUtil.COLOURS) {
            SIGNALS.put(colour, new ActionParameterSignal(colour));
        }
    }

    @Nullable
    public final DyeColor colour;

    private ActionParameterSignal(DyeColor colour) {
        this.colour = colour;
    }

    public static ActionParameterSignal get(DyeColor colour) {
        return colour == null ? EMPTY : SIGNALS.get(colour);
    }

    public static ActionParameterSignal readFromNbt(CompoundNBT nbt) {
        if (nbt.contains("color", Constants.NBT.TAG_ANY_NUMERIC)) {
            return get(DyeColor.byId(nbt.getByte("color")));
        }
        return EMPTY;
    }

    @Override
    public void writeToNbt(CompoundNBT nbt) {
        DyeColor c = colour;
        if (c != null) {
            nbt.putByte("color", (byte) c.getId());
        }
    }

    @Nullable
    public DyeColor getColor() {
        return colour;
    }

    @Override
    public ISprite getSprite() {
        DyeColor c = colour;
        if (c == null) {
            return null;
        } else {
            return BCTransportSprites.getPipeSignal(true, c);
        }
    }

    @Override
    public ActionParameterSignal onClick(IStatementContainer source, IStatement stmt, ItemStack stack,
                                         StatementMouseClick mouse) {
        return null;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof ActionParameterSignal) {
            ActionParameterSignal param = (ActionParameterSignal) object;

            return param.getColor() == getColor();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(getColor());
    }

    @Override
    public ITextComponent getDescription() {
        DyeColor c = colour;
        if (c == null) {
            return null;
        }
//        String format = LocaleUtil.localize("gate.action.pipe.wire");
//        Object[] args = { ColourUtil.getTextFullTooltip(c) };
//        return String.format(format, args);
        return new TranslationTextComponent("gate.action.pipe.wire", ColourUtil.getTextFullTooltipComponent(c));
    }

    @Override
    public String getDescriptionKey() {
        DyeColor c = colour;
        if (c == null) {
            return null;
        }
        return "gate.action.pipe.wire." + c.getName();
    }

    @Override
    public String getUniqueTag() {
        return "buildcraft:pipeWireAction";
    }

    @Override
    public IStatementParameter rotateLeft() {
        return this;
    }

    @Nonnull
    @Override
    public ItemStack getItemStack() {
        return StackUtil.EMPTY;
    }

    @Override
    public IStatementParameter[] getPossible(IStatementContainer source) {
        if (!(source instanceof IGate)) {
            return null;
        }
        IGate gate = (IGate) source;
        List<IStatementParameter> poss = new ArrayList<>(1 + ColourUtil.COLOURS.length);
        poss.add(EMPTY);
        for (DyeColor c : ColourUtil.COLOURS) {
            if (TriggerPipeSignal.doesGateHaveColour(gate, c)) {
                poss.add(get(c));
            }
        }
        return poss.toArray(new IStatementParameter[poss.size()]);
    }
}
