package buildcraft.transport.client.model;

import java.util.Arrays;
import java.util.Objects;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.pluggable.PluggableModelKey;

public abstract class ModelKeyLens extends PluggableModelKey<ModelKeyLens> {
    public final boolean isFilter;
    private final int hash;

    ModelKeyLens(BlockRenderLayer layer, EnumFacing side, boolean filter) {
        super(layer, LensPluggableModel.INSTANCE, side);
        isFilter = filter;
        hash = Arrays.hashCode(new int[] { super.hashCode(), Boolean.hashCode(filter) });
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
        ModelKeyLens other = (ModelKeyLens) obj;
        if (isFilter != other.isFilter) return false;
        return true;
    }

    public static class Cutout extends ModelKeyLens {
        public Cutout(EnumFacing side, boolean filter) {
            super(BlockRenderLayer.CUTOUT, side, filter);
        }
    }

    public static class Translucent extends ModelKeyLens {
        public final EnumDyeColor colour;
        private final int hash;

        public Translucent(EnumFacing side, boolean filter, EnumDyeColor colour) {
            super(BlockRenderLayer.TRANSLUCENT, side, filter);
            this.colour = colour;
            hash = Arrays.hashCode(new int[] { super.hash, Objects.hashCode(colour) });
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
            Translucent other = (Translucent) obj;
            if (colour != other.colour) return false;
            return true;
        }
    }
}
