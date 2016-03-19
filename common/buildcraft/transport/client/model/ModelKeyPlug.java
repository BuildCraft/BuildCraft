package buildcraft.transport.client.model;

import buildcraft.transport.pluggable.PlugPluggable;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;

import buildcraft.api.transport.pluggable.PluggableModelKey;

import java.util.Arrays;

public class ModelKeyPlug extends PluggableModelKey<ModelKeyPlug> {
    public final PlugPluggable.Material material;
    private final int hash;

    public ModelKeyPlug(EnumFacing face, PlugPluggable.Material material) {
        super(EnumWorldBlockLayer.CUTOUT, PlugPluggableModel.INSTANCE, face);
        this.material = material;
        hash = Arrays.hashCode(new int[] { super.hashCode(), material.hashCode() });
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
        ModelKeyPlug other = (ModelKeyPlug) obj;
        if (other.material != material) return false;
        return true;
    }
}
