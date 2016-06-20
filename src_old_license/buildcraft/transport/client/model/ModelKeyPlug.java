package buildcraft.transport.client.model;

import java.util.Arrays;

import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.pluggable.PluggableModelKey;
import buildcraft.transport.pluggable.PlugPluggable;

public class ModelKeyPlug extends PluggableModelKey<ModelKeyPlug> {
    public final PlugPluggable.Material material;
    private final int hash;

    public ModelKeyPlug(EnumFacing face, PlugPluggable.Material material) {
        super(BlockRenderLayer.CUTOUT, PlugPluggableModel.INSTANCE, face);
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
