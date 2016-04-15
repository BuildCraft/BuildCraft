package buildcraft.api;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;

import buildcraft.api.core.BCLog;

/** Stores all of BuildCraft's blocks, from all of its modules. If any of them have been disabled by the user (or it the
 * module is not installed) then they will be null. This is the equivalent of {@link Blocks} */
public class BCBlocks {
    // BC Core
    public static final Block coreDecorated;
    public static final Block coreEngineRedstone;

    // BC Builders

    // BC Energy
    public static final Block energyEngineStirling;
    public static final Block energyEngineCombustion;
    public static final Block energyEngineCreative;

    // BC Factory

    // BC Robotics

    // BC Silicon

    // BC Transport

    static {
        if (!Loader.instance().hasReachedState(LoaderState.INITIALIZATION)) {
            throw new RuntimeException("Accessed BC blocks too early! You can only use them from init onwards!");
        }
        String core = "core";
        coreDecorated = getRegisteredBlock(core, "decorated");
        coreEngineRedstone = getRegisteredBlock(core, "engine_redstone");

        String energy = "energy";
        energyEngineStirling = getRegisteredBlock(energy, "engine_stirling");
        energyEngineCombustion = getRegisteredBlock(energy, "engine_combustion");
        energyEngineCreative = getRegisteredBlock(energy, "engine_creative");
    }

    private static Block getRegisteredBlock(String module, String regName) {
        String modid = "buildcraft" + module;
        Block block = Block.REGISTRY.getObject(new ResourceLocation(modid, regName));
        if (block != null) {
            return block;
        }
        if (Loader.isModLoaded(modid)) {
            // Only info because the item might have been disabled by the user
            BCLog.logger.info("[block-api] Did not find the item " + regName + " dispite the appropriate mod being loaded (" + modid + ")");
        }
        return null;
    }
}
