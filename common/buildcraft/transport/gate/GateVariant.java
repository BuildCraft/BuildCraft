package buildcraft.transport.gate;

import java.util.Objects;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import buildcraft.lib.misc.LocaleUtil;

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

    public GateVariant(NBTTagCompound nbt) {
        this.logic = EnumGateLogic.getByOrdinal(nbt.getByte("logic"));
        this.material = EnumGateMaterial.getByOrdinal(nbt.getByte("material"));
        this.modifier = EnumGateModifier.getByOrdinal(nbt.getByte("modifier"));
        this.numSlots = material.numSlots / modifier.slotDivisor;
        this.numTriggerArgs = modifier.triggerParams;
        this.numActionArgs = modifier.actionParams;
        this.hash = Objects.hash(logic, material, modifier);
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setByte("logic", (byte) logic.ordinal());
        nbt.setByte("material", (byte) material.ordinal());
        nbt.setByte("modifier", (byte) modifier.ordinal());
        return nbt;
    }

    public GateVariant(PacketBuffer buffer) {
        this.logic = EnumGateLogic.getByOrdinal(buffer.readUnsignedByte());
        this.material = EnumGateMaterial.getByOrdinal(buffer.readUnsignedByte());
        this.modifier = EnumGateModifier.getByOrdinal(buffer.readUnsignedByte());
        this.numSlots = material.numSlots / modifier.slotDivisor;
        this.numTriggerArgs = modifier.triggerParams;
        this.numActionArgs = modifier.actionParams;
        this.hash = Objects.hash(logic, material, modifier);
    }

    public void writeToBuffer(PacketBuffer buffer) {
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
            return String.format(gateName, materialName, logicName);
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
