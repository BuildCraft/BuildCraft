package buildcraft.lib;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.text.TextFormatting;

import buildcraft.lib.misc.ColourUtil;

/** Configuration file for lib. In order to keep lib as close to being just a library mod as possible, these are not set
 * by a config file, but instead by BC Core. Feel free to set them yourself, from your own configs, if you do not depend
 * on BC COre itself, and it might not be loaded in the mod environment. */
public class BCLibConfig {
    /** If true then items and blocks will display the colour of an item (one of {@link EnumDyeColor}) with the correct
     * {@link TextFormatting} colour value.<br>
     * This changes the behaviour of {@link ColourUtil#convertColourToTextFormat(EnumDyeColor)}. */
    public static boolean useColouredLabels = true;

    /** The lifespan (in seconds) that spawned items will have, when dropped by a quarry or builder (etc) */
    public static int itemLifespan = 60;

    /** If true then ItemRenderUtil.renderItemStack will use the facing parameter to rotate the item */
    public static RenderRotation rotateTravelingItems = RenderRotation.ENABLED;

    public enum RenderRotation {
        DISABLED {
            @Override
            public EnumFacing changeFacing(EnumFacing dir) {
                return EnumFacing.EAST;
            }
        },
        HORIZONTALS_ONLY {
            @Override
            public EnumFacing changeFacing(EnumFacing dir) {
                return dir.getAxis() == Axis.Y ? EnumFacing.EAST : dir;
            }
        },
        ENABLED {
            @Override
            public EnumFacing changeFacing(EnumFacing dir) {
                return dir;
            }
        };

        public abstract EnumFacing changeFacing(EnumFacing dir);
    }
}
