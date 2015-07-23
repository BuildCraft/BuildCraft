package buildcraft.api.tiles;

import java.util.List;

import net.minecraft.util.EnumFacing;

/** I would like to ask that this interface is not called by any non-Creative Mode-only item, and especially not
 * computer mods. This is because often, the debug information can and will let you "cheat", similar to how F3 gives you
 * debug information about Minecraft's inner engine workings. */
public interface IDebuggable {
    /** Get the debug information from a tile entity as a list of strings, used for the F3 debug menu
     * 
     * @param info The List debug strings should be output to. */
    void getDebugInfo(List<String> left, List<String> right, EnumFacing side);
}
