package buildcraft.transport.client.model;

import java.util.Objects;

import com.google.common.collect.ImmutableSet;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;

import buildcraft.api.gates.GateExpansionModelKey;
import buildcraft.api.gates.IGateExpansion;
import buildcraft.api.transport.pluggable.PluggableModelKey;
import buildcraft.transport.gates.GateDefinition.GateLogic;
import buildcraft.transport.gates.GateDefinition.GateMaterial;

public class ModelKeyGate extends PluggableModelKey<ModelKeyGate> {
    public final GateMaterial material;
    public final GateLogic logic;
    public final ImmutableSet<GateExpansionModelKey<?>> expansions;
    public final boolean lit;
    public final int hash;

    public ModelKeyGate(EnumFacing side, GateMaterial material, GateLogic logic, boolean lit, IGateExpansion[] expansions) {
        super(EnumWorldBlockLayer.CUTOUT, GatePluggableModel.INSTANCE, side);
        this.material = material;
        this.logic = logic;
        this.lit = lit;
        ImmutableSet.Builder<GateExpansionModelKey<?>> builder = ImmutableSet.builder();
        for (IGateExpansion exp : expansions) {
            if (exp != null) {
                GateExpansionModelKey<?> key = exp.getRenderModelKey(layer);
                if (key != null) {
                    builder.add(key);
                }
            }
        }
        this.expansions = builder.build();
        this.hash = Objects.hash(this.material, this.logic, this.lit, this.expansions);
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
        ModelKeyGate other = (ModelKeyGate) obj;
        if (!expansions.equals(other.expansions)) return false;
        if (logic != other.logic) return false;
        if (material != other.material) return false;
        if (lit != other.lit) return false;
        return true;
    }
}
