package buildcraft.api.tiles;

import java.util.List;

import net.minecraft.util.EnumFacing;

/** I would like to ask that this interface is not called by any non-Creative Mode-only item, and especially not
 * computer mods. This is because often, the debug information can and will let you "cheat", similar to how F3 gives you
 * debug information about Minecraft's inner engine workings. */
public interface IDebuggable {
    /** Get the debug information from a tile entity as a list of strings, used for the F3 debug menu. The left and
     * right parameters correspond to the sides of the F3 screen.
     * 
     * @param side The side the block was clicked on, may be null if we don't know, or is the "centre" side */
    void getDebugInfo(List<String> left, List<String> right, EnumFacing side);
}
