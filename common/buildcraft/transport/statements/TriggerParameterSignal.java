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

public class TriggerParameterSignal implements IStatementParameter {

    public static final TriggerParameterSignal EMPTY = new TriggerParameterSignal(false, null);
    private static final Map<DyeColor, TriggerParameterSignal> SIGNALS_OFF = new EnumMap<>(DyeColor.class);
    private static final Map<DyeColor, TriggerParameterSignal> SIGNALS_ON = new EnumMap<>(DyeColor.class);

    static {
        for (DyeColor c : DyeColor.values()) {
            SIGNALS_OFF.put(c, new TriggerParameterSignal(false, c));
            SIGNALS_ON.put(c, new TriggerParameterSignal(true, c));
        }
    }

    public final boolean active;
    @Nullable
    public final DyeColor colour;

    private TriggerParameterSignal(boolean active, DyeColor colour) {
        this.active = active;
        this.colour = colour;
    }

    public static TriggerParameterSignal get(boolean active, DyeColor colour) {
        if (colour == null) return EMPTY;
        return active ? SIGNALS_ON.get(colour) : SIGNALS_OFF.get(colour);
    }

    public static TriggerParameterSignal readFromNbt(NbtCompound nbt) {
        if (nbt.contains("color", NbtElement.NUMBER_TYPE)) {
            DyeColor colour = DyeColor.byId(nbt.getByte("color") & 0xFF);
            boolean active = nbt.getBoolean("active");
            return get(active, colour);
        }
        return EMPTY;
    }

    @Override
    public void writeToNbt(NbtCompound nbt) {
        if (colour != null) {
            nbt.putByte("color", (byte) colour.getId());
            nbt.putBoolean("active", active);
        }
    }

    @Nonnull
    @Override
    public ItemStack getItemStack() {
        return StackUtil.EMPTY;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public ISprite getSprite() {
        return colour == null ? null : BCTransportSprites.getPipeSignal(active, colour);
    }

    @Override
    public TriggerParameterSignal onClick(IStatementContainer source, IStatement stmt, ItemStack stack,
        StatementMouseClick mouse) {
        return null;
    }

    @Override
    public String getDescription() {
        return colour == null ? null
            : "gate.trigger.pipe.wire." + (active ? "active" : "inactive") + "." + colour.getName();
    }

    @Override
    public String getUniqueTag() {
        return "buildcraft:pipeWireTrigger";
    }

    @Override
    public IStatementParameter rotateLeft() {
        return this;
    }

    @Override
    public TriggerParameterSignal[] getPossible(IStatementContainer source) {
        if (!(source instanceof IGate)) return null;
        IGate gate = (IGate) source;
        List<TriggerParameterSignal> poss = new ArrayList<>();
        poss.add(EMPTY);
        for (DyeColor c : DyeColor.values()) {
            if (TriggerPipeSignal.doesGateHaveColour(gate, c)) {
                poss.add(get(false, c));
                poss.add(get(true, c));
            }
        }
        return poss.toArray(new TriggerParameterSignal[0]);
    }
}
