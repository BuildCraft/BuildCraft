package ct.buildcraft.api.items;

import java.util.List;

import javax.annotation.Nonnull;

import ct.buildcraft.api.core.IBox;
import ct.buildcraft.api.core.IZone;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/** Created by asie on 2/28/15. */
public interface IMapLocation extends INamedItem {
    enum MapLocationType {
        CLEAN,
        SPOT,
        AREA,
        PATH,
        ZONE,
        /** Like PATH but repeats around in a loop. */
        PATH_REPEATING;

        public final int meta = ordinal();

        public static MapLocationType getFromStack(@Nonnull ItemStack stack) {
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains("kind")) {
                int kind = tag.getByte("kind");
                return switch (kind) {
                    case 0 -> SPOT;
                    case 1 -> AREA;
                    case 2 -> PATH;
                    case 3 -> ZONE;
                    case 4 -> PATH_REPEATING;
                    default -> CLEAN;
                };
            }

            int dam = stack.getDamageValue();
            if (dam < 0 || dam >= values().length) {
                return MapLocationType.CLEAN;
            }
            return values()[dam];
        }

        public void setToStack(@Nonnull ItemStack stack) {
            stack.setDamageValue(meta);
            CompoundTag tag = stack.getTag();

            if (this == CLEAN) {
                if (tag != null) {
                    tag.remove("kind");
                    tag.remove("Damage");
                    if (tag.isEmpty()) {
                        stack.setTag(null);
                    }
                }
                return;
            }

            tag = stack.getOrCreateTag();
            tag.putByte("kind", switch (this) {
                case SPOT -> (byte) 0;
                case AREA -> (byte) 1;
                case PATH -> (byte) 2;
                case ZONE -> (byte) 3;
                case PATH_REPEATING -> (byte) 4;
                case CLEAN -> (byte) -1;
            });
        }
    }

    /** This function can be used for SPOT types.
     * 
     * @param stack
     * @return The point representing the map location. */
    BlockPos getPoint(@Nonnull ItemStack stack);

    /** This function can be used for SPOT and AREA types.
     * 
     * @param stack
     * @return The box representing the map location. */
    IBox getBox(@Nonnull ItemStack stack);

    /** This function can be used for SPOT, AREA and ZONE types. The PATH type needs to be handled separately.
     * 
     * @param stack
     * @return An IZone representing the map location - also an instance of IBox for SPOT and AREA types. */
    IZone getZone(@Nonnull ItemStack stack);

    /** This function can be used for SPOT and PATH types.
     * 
     * @param stack
     * @return A list of BlockPoses representing the path the Map Location stores. */
    List<BlockPos> getPath(@Nonnull ItemStack stack);

    /** This function can be used for SPOT types only.
     * 
     * @param stack
     * @return The side of the spot. */
    Direction getPointSide(@Nonnull ItemStack stack);
}
