package buildcraft.transport.client.model.key;

import java.util.Objects;

import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.pluggable.PluggableModelKey;
import buildcraft.transport.client.model.plug.PlugGateBaker;
import buildcraft.transport.gate.GateVariant;

public class KeyPlugGate extends PluggableModelKey<KeyPlugGate> {
    public final GateVariant variant;
    public final int hash;

    public KeyPlugGate(EnumFacing side, GateVariant variant) {
        super(BlockRenderLayer.CUTOUT, PlugGateBaker.INSTANCE, side);
        this.variant = variant;
        this.hash = Objects.hash(variant, side);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        KeyPlugGate other = (KeyPlugGate) obj;
        return side == other.side && variant.equals(other.variant);
    }
}
