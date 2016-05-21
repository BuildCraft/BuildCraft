package buildcraft.transport.client.model;

import java.util.Arrays;
import java.util.Objects;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;

import buildcraft.api.transport.pluggable.PluggableModelKey;

public abstract class ModelKeyLens extends PluggableModelKey<ModelKeyLens> {
    public final boolean isFilter;
    private final int hash;

    ModelKeyLens(EnumWorldBlockLayer layer, EnumFacing side, boolean filter) {
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
            super(EnumWorldBlockLayer.CUTOUT, side, filter);
        }
    }

    public static class Translucent extends ModelKeyLens {
        public final EnumDyeColor colour;
        private final int hash;

        public Translucent(EnumFacing side, boolean filter, EnumDyeColor colour) {
            super(EnumWorldBlockLayer.TRANSLUCENT, side, filter);
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
