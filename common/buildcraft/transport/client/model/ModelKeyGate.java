package buildcraft.transport.client.model;

import java.util.Objects;

import com.google.common.collect.ImmutableSet;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;

import buildcraft.api.gates.IGateExpansion;
import buildcraft.api.transport.pluggable.PluggableModelKey;
import buildcraft.transport.gates.GateDefinition.GateLogic;
import buildcraft.transport.gates.GateDefinition.GateMaterial;

public class ModelKeyGate extends PluggableModelKey<ModelKeyGate> {
    public final GateMaterial material;
    public final GateLogic logic;
    public final ImmutableSet<ModelKeyGateExpansion> expansions;
    public final int hash;

    public ModelKeyGate(EnumFacing side, GateMaterial material, GateLogic logic, IGateExpansion[] expansions) {
        super(EnumWorldBlockLayer.CUTOUT, GatePluggableModel.INSTANCE, side);
        this.material = material;
        this.logic = logic;
        // TODO: Expansions
        this.expansions = ImmutableSet.of();
        this.hash = Objects.hash(this.material, this.logic, this.expansions);
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
        return true;
    }
}
