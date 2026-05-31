/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.silicon.gate;

import java.util.Objects;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;

import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.StringUtilBC;

public class GateVariant {
    public final EnumGateLogic logic;
    public final EnumGateMaterial material;
    public final EnumGateModifier modifier;
    public final int numSlots;
    public final int numTriggerArgs, numActionArgs;
    private final int hash;

    public GateVariant(EnumGateLogic logic, EnumGateMaterial material, EnumGateModifier modifier) {
        this.logic = logic;
        this.material = material;
        this.modifier = modifier;
        this.numSlots = material.numSlots / modifier.slotDivisor;
        this.numTriggerArgs = modifier.triggerParams;
        this.numActionArgs = modifier.actionParams;
        this.hash = Objects.hash(logic, material, modifier);
    }

    public GateVariant(NbtCompound nbt) {
        this.logic = EnumGateLogic.getByOrdinal(nbt.getByte("logic"));
        this.material = EnumGateMaterial.getByOrdinal(nbt.getByte("material"));
        this.modifier = EnumGateModifier.getByOrdinal(nbt.getByte("modifier"));
        this.numSlots = material.numSlots / modifier.slotDivisor;
        this.numTriggerArgs = modifier.triggerParams;
        this.numActionArgs = modifier.actionParams;
        this.hash = Objects.hash(logic, material, modifier);
    }

    public NbtCompound writeToNBT() {
        NbtCompound nbt = new NbtCompound();
        nbt.putByte("logic", (byte) logic.ordinal());
        nbt.putByte("material", (byte) material.ordinal());
        nbt.putByte("modifier", (byte) modifier.ordinal());
        return nbt;
    }

    public GateVariant(PacketByteBuf buffer) {
        this.logic = EnumGateLogic.getByOrdinal(buffer.readUnsignedByte());
        this.material = EnumGateMaterial.getByOrdinal(buffer.readUnsignedByte());
        this.modifier = EnumGateModifier.getByOrdinal(buffer.readUnsignedByte());
        this.numSlots = material.numSlots / modifier.slotDivisor;
        this.numTriggerArgs = modifier.triggerParams;
        this.numActionArgs = modifier.actionParams;
        this.hash = Objects.hash(logic, material, modifier);
    }

    public void writeToBuffer(PacketByteBuf buffer) {
        buffer.writeByte(logic.ordinal());
        buffer.writeByte(material.ordinal());
        buffer.writeByte(modifier.ordinal());
    }

    public String getVariantName() {
        if (material.canBeModified) {
            return material.tag + "_" + logic.tag + "_" + modifier.tag;
        } else {
            return material.tag;
        }
    }

    public String getLocalizedName() {
        if (material == EnumGateMaterial.CLAY_BRICK) {
            return LocaleUtil.localize("gate.name.basic");
        } else {
            String gateName = LocaleUtil.localize("gate.name");
            String materialName = LocaleUtil.localize("gate.material." + material.tag);
            Object logicName = LocaleUtil.localize("gate.logic." + logic.tag);
            return StringUtilBC.formatSafe(gateName, materialName, logicName);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != getClass()) return false;
        GateVariant other = (GateVariant) obj;
        return other.logic == logic//
            && other.material == material//
            && other.modifier == modifier;
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
