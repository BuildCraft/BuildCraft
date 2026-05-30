/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.transport.statements;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.DyeColor;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.gates.IGate;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementMouseClick;

import buildcraft.lib.misc.StackUtil;

import buildcraft.transport.BCTransportSprites;

public class ActionParameterSignal implements IStatementParameter {

    public static final ActionParameterSignal EMPTY = new ActionParameterSignal(null);
    private static final Map<DyeColor, ActionParameterSignal> SIGNALS = new EnumMap<>(DyeColor.class);

    static {
        for (DyeColor colour : DyeColor.values()) {
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

    @Nullable
    public DyeColor getColor() {
        return colour;
    }

    public static ActionParameterSignal readFromNbt(NbtCompound nbt) {
        if (nbt.contains("color", NbtElement.NUMBER_TYPE)) {
            DyeColor colour = DyeColor.byId(nbt.getByte("color") & 0xFF);
            return get(colour);
        }
        return EMPTY;
    }

    @Override
    public void writeToNbt(NbtCompound nbt) {
        if (colour != null) {
            nbt.putByte("color", (byte) colour.getId());
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public ISprite getSprite() {
        return colour == null ? null : BCTransportSprites.getPipeSignal(true, colour);
    }

    @Override
    public ActionParameterSignal onClick(IStatementContainer source, IStatement stmt, ItemStack stack,
        StatementMouseClick mouse) {
        return null;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof ActionParameterSignal) {
            return Objects.equals(((ActionParameterSignal) object).colour, colour);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(colour);
    }

    @Override
    public String getDescription() {
        return colour == null ? null : "gate.action.pipe.wire." + colour.getName();
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
        List<IStatementParameter> poss = new ArrayList<>();
        poss.add(EMPTY);
        for (DyeColor c : DyeColor.values()) {
            if (TriggerPipeSignal.doesGateHaveColour(gate, c)) {
                poss.add(get(c));
            }
        }
        return poss.toArray(new IStatementParameter[0]);
    }
}
