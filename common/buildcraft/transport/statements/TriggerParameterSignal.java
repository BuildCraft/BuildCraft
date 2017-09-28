/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.statements;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.gates.IGate;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementMouseClick;

import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.net.PacketBufferBC;

import buildcraft.transport.BCTransportSprites;

public class TriggerParameterSignal implements IStatementParameter {

    public static final TriggerParameterSignal EMPTY;
    private static final Map<EnumDyeColor, TriggerParameterSignal> SIGNALS_OFF, SIGNALS_ON;

    static {
        EMPTY = new TriggerParameterSignal(false, null);
        SIGNALS_OFF = new EnumMap<>(EnumDyeColor.class);
        SIGNALS_ON = new EnumMap<>(EnumDyeColor.class);
        for (EnumDyeColor colour : ColourUtil.COLOURS) {
            SIGNALS_OFF.put(colour, new TriggerParameterSignal(false, colour));
            SIGNALS_ON.put(colour, new TriggerParameterSignal(true, colour));
        }
    }

    public static TriggerParameterSignal get(boolean active, EnumDyeColor colour) {
        if (colour == null) {
            return EMPTY;
        }
        return new TriggerParameterSignal(active, colour);
    }

    public static TriggerParameterSignal readFromNbt(NBTTagCompound nbt) {
        if (nbt.hasKey("color", Constants.NBT.TAG_ANY_NUMERIC)) {
            EnumDyeColor colour = EnumDyeColor.byMetadata(nbt.getByte("color"));
            boolean active = nbt.getBoolean("active");
            return get(active, colour);
        } else {
            return EMPTY;
        }
    }

    @Override
    public void writeToNbt(NBTTagCompound nbt) {
        if (colour != null) {
            nbt.setByte("color", (byte) colour.getMetadata());
            nbt.setBoolean("active", active);
        }
    }

    public static TriggerParameterSignal readFromBuf(PacketBuffer buffer) {
        PacketBufferBC buf = PacketBufferBC.asPacketBufferBc(buffer);
        EnumDyeColor colour = MessageUtil.readEnumOrNull(buf, EnumDyeColor.class);
        if (colour == null) {
            return EMPTY;
        } else {
            return get(buf.readBoolean(), colour);
        }
    }

    @Override
    public void writeToBuf(PacketBuffer buffer) {
        MessageUtil.writeEnumOrNull(buffer, colour);
        if (colour != null) {
            buffer.writeBoolean(active);
        }
    }

    public final boolean active;

    @Nullable
    public final EnumDyeColor colour;

    private TriggerParameterSignal(boolean active, EnumDyeColor colour) {
        this.active = active;
        this.colour = colour;
    }

    @Override
    public ItemStack getItemStack() {
        return StackUtil.EMPTY;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ISprite getSprite() {
        if (colour == null) {
            return null;
        }
        return BCTransportSprites.getPipeSignal(active, colour);
    }

    @Override
    public TriggerParameterSignal onClick(IStatementContainer source, IStatement stmt, ItemStack stack,
        StatementMouseClick mouse) {
        return null;
    }

    @Override
    public String getDescription() {
        if (colour == null) {
            return null;
        }
        return String.format(LocaleUtil.localize("gate.trigger.pipe.wire." + (active ? "active" : "inactive")),
            ColourUtil.getTextFullTooltip(colour));
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
        if (!(source instanceof IGate)) {
            return null;
        }
        IGate gate = (IGate) source;
        List<TriggerParameterSignal> poss = new ArrayList<>(ColourUtil.COLOURS.length * 2 + 1);
        poss.add(EMPTY);
        for (EnumDyeColor c : ColourUtil.COLOURS) {
            if (TriggerPipeSignal.doesGateHaveColour(gate, c)) {
                poss.add(get(true, c));
                poss.add(get(false, c));
            }
        }
        return poss.toArray(new TriggerParameterSignal[poss.size()]);
    }
}
